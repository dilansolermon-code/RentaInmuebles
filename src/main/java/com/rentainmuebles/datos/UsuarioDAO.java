package com.rentainmuebles.datos;

import com.rentainmuebles.modelo.Administrador;
import com.rentainmuebles.modelo.Banco;
import com.rentainmuebles.modelo.Cliente;
import com.rentainmuebles.modelo.Perfil;
import com.rentainmuebles.modelo.Propietario;
import com.rentainmuebles.modelo.Usuario;
import com.rentainmuebles.util.Seguridad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla usuario, sus tablas hijas (cliente, propietario, administrador) y sus cuentas. */
public class UsuarioDAO {

    // Une usuario con sus tres tablas hijas. Si aparece en varias, el rol principal es:
    // administrador, luego propietario, luego cliente.
    private static final String SELECT_BASE =
          "SELECT u.id, u.nombre, u.telefono, u.email, u.contrasena, u.estado_cuenta, "
        + "       a.id AS es_admin, a.cargo, a.nivel_acceso, "
        + "       p.id AS es_prop, p.tipo_persona, p.calificacion AS calif_prop, "
        + "       c.id AS es_cli, c.calificacion AS calif_cli, ci.nombre AS ciudad_residencia, "
        + "       CONCAT_WS(', ', CASE WHEN a.id IS NOT NULL THEN 'Administrador' END, "
        + "                       CASE WHEN p.id IS NOT NULL THEN 'Propietario' END, "
        + "                       CASE WHEN c.id IS NOT NULL THEN 'Cliente' END) AS roles "
        + "FROM usuario u "
        + "LEFT JOIN administrador a ON a.id = u.id "
        + "LEFT JOIN propietario p ON p.id = u.id "
        + "LEFT JOIN cliente c ON c.id = u.id "
        + "LEFT JOIN ciudad ci ON ci.id = c.id_ciudad_residencia ";

    // ======================= INICIAR SESIÓN =======================

    /**
     * Busca todas las cuentas (roles) que tiene un email.
     * Una misma persona puede ser Cliente y Propietario a la vez, así que la lista
     * puede traer 1, 2 o 3 objetos: uno por cada rol (polimorfismo: todos son Usuario).
     * La verificación de la contraseña la hace Usuario.iniciarSesion().
     */
    public List<Usuario> buscarRolesPorEmail(String email) throws SQLException {
        String sql = SELECT_BASE + "WHERE LOWER(u.email) = LOWER(TRIM(?)) ORDER BY u.id";
        List<Usuario> roles = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {                                 // normalmente hay una sola fila
                    String[] posibles = {"admin", "propietario", "cliente"};
                    for (String rol : posibles) {
                        Usuario u = crearConRol(rs, rol);           // null si no tiene ese rol
                        if (u != null) {
                            roles.add(u);
                        }
                    }
                }
            }
        }
        return roles;
    }

    // ======================= REGISTRARSE (CU-01) =======================

    /** true si ya hay un usuario con ese email. */
    public boolean emailExiste(String email) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM usuario WHERE LOWER(email) = LOWER(?)")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Crea un usuario nuevo con su rol y su cuenta bancaria, en una sola transacción.
     * tipo: "cliente" o "propietario". Devuelve el id asignado. La cuenta nace "activo".
     */
    public int registrar(String tipo, String nombre, String telefono, String email, String contrasena,
                         int idCiudadResidencia, String tipoPersona,
                         int idBanco, String numeroCta, String tipoCta) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);                              // inicia la transacción
            try {
                // 1. Tabla padre: usuario (la contraseña se guarda cifrada)
                int id;
                String sqlUsuario = "INSERT INTO usuario (id, nombre, telefono, email, contrasena, estado_cuenta) "
                                  + "VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM usuario), ?, ?, ?, ?, 'activo') RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                    ps.setString(1, nombre);
                    ps.setString(2, telefono);
                    ps.setString(3, email);
                    ps.setString(4, Seguridad.cifrar(contrasena));
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                }

                // 2. Tabla hija según el tipo de cuenta
                insertarRol(con, tipo, id, idCiudadResidencia, tipoPersona);

                // 3. Cuenta bancaria
                guardarCuenta(con, tipo, id, idBanco, numeroCta, tipoCta);

                con.commit();                                      // confirma todo
                return id;
            } catch (SQLException e) {
                con.rollback();                                    // deshace todo si algo falló
                throw e;
            }
        }
    }

    /**
     * Agrega un rol nuevo (cliente o propietario) a un usuario que ya existe,
     * con su cuenta bancaria. Así la persona conserva un solo usuario y un solo correo.
     */
    public void agregarRol(int idUsuario, String tipo, int idCiudadResidencia, String tipoPersona,
                           int idBanco, String numeroCta, String tipoCta) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);                              // todo o nada
            try {
                insertarRol(con, tipo, idUsuario, idCiudadResidencia, tipoPersona);
                guardarCuenta(con, tipo, idUsuario, idBanco, numeroCta, tipoCta);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ======================= MI PERFIL =======================

    /** Datos del usuario y de su cuenta bancaria. rol: "cliente" o "propietario". */
    public Perfil obtenerPerfil(int idUsuario, String rol) throws SQLException {
        String tabla = rol.equals("cliente") ? "cuenta_cliente" : "cuenta_propietario";
        String dueno = rol.equals("cliente") ? "id_cliente" : "id_propietario";
        // Subconsulta: la cuenta más reciente del usuario (si tiene)
        String sql = "SELECT u.nombre, u.telefono, u.email, k.id_banco, k.numero_cta, k.tipo_cta "
                   + "FROM usuario u "
                   + "LEFT JOIN (SELECT * FROM " + tabla + " WHERE " + dueno + " = ? ORDER BY id DESC LIMIT 1) k ON true "
                   + "WHERE u.id = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Perfil(rs.getString("nombre"), rs.getString("telefono"), rs.getString("email"),
                                      rs.getInt("id_banco"), rs.getString("numero_cta"), rs.getString("tipo_cta"));
                }
            }
        }
        return null;
    }

    /**
     * Actualiza los datos de contacto y la cuenta bancaria.
     * Si numeroCta es null, se conserva el número de cuenta actual.
     */
    public void actualizarPerfil(int idUsuario, String rol, String nombre, String telefono, String email,
                                 int idBanco, String numeroCta, String tipoCta) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario SET nombre = ?, telefono = ?, email = ? WHERE id = ?")) {
                    ps.setString(1, nombre);
                    ps.setString(2, telefono);
                    ps.setString(3, email);
                    ps.setInt(4, idUsuario);
                    ps.executeUpdate();
                }
                guardarCuenta(con, rol, idUsuario, idBanco, numeroCta, tipoCta);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ======================= ADMINISTRADOR (CU-08) =======================

    /** Busca cuentas por nombre o email (máximo 50), con la descripción de todos sus roles. */
    public List<Usuario> buscar(String texto) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE u.nombre ILIKE ? OR u.email ILIKE ? "   // ILIKE: sin importar mayúsculas
                   + "ORDER BY u.id LIMIT 50";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");                   // % = cualquier texto antes o después
            ps.setString(2, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = crearUsuario(rs);
                    if (u != null) {
                        u.cerrarSesion();                          // no guardar huellas en el listado
                        lista.add(u);
                    }
                }
            }
        }
        return lista;
    }

    /** Suspende o restaura una cuenta. nuevoEstado: "activo" o "inactivo". Devuelve true si la cambió. */
    public boolean cambiarEstadoCuenta(int idUsuario, String nuevoEstado) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("UPDATE usuario SET estado_cuenta = ? WHERE id = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /** Cuenta bancaria más reciente de un propietario, para abonarle las rentas. null si no tiene. */
    public Banco obtenerCuentaPropietario(int idPropietario) throws SQLException {
        String sql = "SELECT b.id, b.nombre, k.numero_cta, k.tipo_cta "
                   + "FROM cuenta_propietario k JOIN banco b ON b.id = k.id_banco "
                   + "WHERE k.id_propietario = ? ORDER BY k.id DESC LIMIT 1";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPropietario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Banco(rs.getInt("id"), rs.getString("nombre"),
                                     rs.getString("numero_cta"), rs.getString("tipo_cta"));
                }
            }
        }
        return null;
    }

    // ======================= MÉTODOS DE APOYO =======================

    /**
     * Convierte la fila actual en el objeto de su rol principal (polimorfismo):
     * administrador, luego propietario, luego cliente. Se usa en el listado del administrador.
     */
    private Usuario crearUsuario(ResultSet rs) throws SQLException {
        Usuario u = crearConRol(rs, "admin");
        if (u == null) u = crearConRol(rs, "propietario");
        if (u == null) u = crearConRol(rs, "cliente");
        return u;                                                  // null si no tiene ningún rol
    }

    /**
     * Crea el objeto del rol pedido ("admin", "propietario" o "cliente") con los datos de la fila,
     * o devuelve null si el usuario no tiene ese rol.
     */
    private Usuario crearConRol(ResultSet rs, String rol) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String telefono = rs.getString("telefono");
        String email = rs.getString("email");
        String clave = rs.getString("contrasena");
        Usuario u = null;
        if (rol.equals("admin") && rs.getObject("es_admin") != null) {
            u = new Administrador(id, nombre, telefono, email, clave, rs.getString("cargo"), rs.getInt("nivel_acceso"));
        } else if (rol.equals("propietario") && rs.getObject("es_prop") != null) {
            u = new Propietario(id, nombre, telefono, email, clave, rs.getString("tipo_persona"), rs.getDouble("calif_prop"));
        } else if (rol.equals("cliente") && rs.getObject("es_cli") != null) {
            u = new Cliente(id, nombre, telefono, email, clave, rs.getString("ciudad_residencia"), rs.getDouble("calif_cli"));
        }
        if (u != null) {
            u.setDescripcionRoles(rs.getString("roles"));
            u.setEstadoCuenta(rs.getString("estado_cuenta"));     // columna nueva: activo / inactivo
        }
        return u;
    }

    /** Inserta la fila de la tabla hija: cliente o propietario (calificación inicial: 5). */
    private void insertarRol(Connection con, String tipo, int id,
                             int idCiudadResidencia, String tipoPersona) throws SQLException {
        if (tipo.equals("cliente")) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cliente (id, calificacion, id_ciudad_residencia) VALUES (?, 5, ?)")) {
                ps.setInt(1, id);
                ps.setInt(2, idCiudadResidencia);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO propietario (id, tipo_persona, calificacion) VALUES (?, ?, 5)")) {
                ps.setInt(1, id);
                ps.setString(2, tipoPersona);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Guarda la cuenta bancaria en cuenta_cliente o cuenta_propietario según el rol.
     * Si ya tiene cuenta la actualiza; si no, la crea. Si numeroCta es null conserva el número.
     */
    private void guardarCuenta(Connection con, String rol, int idUsuario,
                               int idBanco, String numeroCta, String tipoCta) throws SQLException {
        String tabla = rol.equals("cliente") ? "cuenta_cliente" : "cuenta_propietario";
        String dueno = rol.equals("cliente") ? "id_cliente" : "id_propietario";

        String actualizar = "UPDATE " + tabla + " SET id_banco = ?, tipo_cta = ?, "
                          + "numero_cta = COALESCE(?, numero_cta) WHERE " + dueno + " = ?";
        try (PreparedStatement ps = con.prepareStatement(actualizar)) {
            ps.setInt(1, idBanco);
            ps.setString(2, tipoCta);
            ps.setString(3, numeroCta);                            // null = deja el número que tenía
            ps.setInt(4, idUsuario);
            if (ps.executeUpdate() > 0 || numeroCta == null) {
                return;                                            // ya tenía cuenta, o no hay número para crearla
            }
        }
        String insertar = "INSERT INTO " + tabla + " (id, numero_cta, tipo_cta, id_banco, " + dueno + ") "
                        + "VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM " + tabla + "), ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(insertar)) {
            ps.setString(1, numeroCta);
            ps.setString(2, tipoCta);
            ps.setInt(3, idBanco);
            ps.setInt(4, idUsuario);
            ps.executeUpdate();
        }
    }
}
