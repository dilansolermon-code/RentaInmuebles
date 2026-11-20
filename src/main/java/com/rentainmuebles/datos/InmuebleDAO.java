package com.rentainmuebles.datos;

import com.rentainmuebles.modelo.Inmueble;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla inmueble (con su ciudad, precio y propietario).
 * Un inmueble se asocia a su propietario en propietario_inmueble (relación Asocia).
 * Su estado_aprobacion dice qué ve cada actor:
 *   pendiente -> espera revisión, aprobada -> la ven los clientes,
 *   rechazado -> no se publica, retirado -> el propietario o el admin lo quitó.
 */
public class InmuebleDAO {

    // Parte común de las consultas
    private static final String SELECT_BASE =
          "SELECT i.id, i.direccion, i.area_mts, i.numero_habitaciones, i.capacidad, i.estado_aprobacion, "
        + "       c.nombre AS ciudad, pi.precio_noche, pi.id_propietario, "
        + "       u.nombre AS propietario, u.email AS email_propietario "
        + "FROM inmueble i "
        + "JOIN ciudad c ON c.id = i.id_ciudad "
        + "JOIN propietario_inmueble pi ON pi.id_inmueble = i.id "
        + "LEFT JOIN usuario u ON u.id = pi.id_propietario ";

    // Condición que se repite: solo inmuebles aprobados (lo único que ve el cliente)
    private static final String SOLO_APROBADOS = " i.estado_aprobacion = '" + Inmueble.APROBADA + "' ";

    // ======================= CLIENTE =======================

    /**
     * Lista los inmuebles APROBADOS de una ciudad con capacidad para cierto número de personas.
     * Si ciudad está vacía, trae todas las ciudades.
     */
    public List<Inmueble> buscar(String ciudad, int personas) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE" + SOLO_APROBADOS
                   + "  AND (? = '' OR c.nombre = ?) AND i.capacidad >= ? "
                   + "ORDER BY i.id";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ciudad);
            ps.setString(2, ciudad);
            ps.setInt(3, personas);
            return leerLista(ps);
        }
    }

    /** Un inmueble APROBADO por su id (detalle y pago del cliente). null si no existe o no está aprobado. */
    public Inmueble buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE i.id = ? AND" + SOLO_APROBADOS;
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            List<Inmueble> lista = leerLista(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    // ======================= PROPIETARIO =======================

    /** Inmuebles de un propietario, en cualquier estado (también los retirados). */
    public List<Inmueble> listarPorPropietario(int idPropietario) throws SQLException {
        String sql = SELECT_BASE + "WHERE pi.id_propietario = ? ORDER BY i.id";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPropietario);
            return leerLista(ps);
        }
    }

    /** Un inmueble solo si pertenece a ese propietario (para editarlo). */
    public Inmueble buscarDelPropietario(int idPropietario, int idInmueble) throws SQLException {
        String sql = SELECT_BASE + "WHERE pi.id_propietario = ? AND i.id = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPropietario);
            ps.setInt(2, idInmueble);
            List<Inmueble> lista = leerLista(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    /**
     * Registra un inmueble y lo asocia al propietario (inmueble + propietario_inmueble) en una transacción.
     * Queda 'pendiente' hasta que el administrador lo apruebe.
     */
    public int registrar(int idPropietario, String direccion, double areaMts, int habitaciones,
                         int capacidad, int idCiudad, double precioNoche) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int idInmueble;
                String sqlInmueble = "INSERT INTO inmueble (id, direccion, area_mts, numero_habitaciones, capacidad, id_ciudad, estado_aprobacion) "
                                   + "VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM inmueble), ?, ?, ?, ?, ?, ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(sqlInmueble)) {
                    ps.setString(1, direccion);
                    ps.setDouble(2, areaMts);
                    ps.setInt(3, habitaciones);
                    ps.setInt(4, capacidad);
                    ps.setInt(5, idCiudad);
                    ps.setString(6, Inmueble.PENDIENTE);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        idInmueble = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO propietario_inmueble (id_propietario, id_inmueble, precio_noche) VALUES (?, ?, ?)")) {
                    ps.setInt(1, idPropietario);
                    ps.setInt(2, idInmueble);
                    ps.setDouble(3, precioNoche);
                    ps.executeUpdate();
                }
                con.commit();
                return idInmueble;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Actualiza los datos y el precio de un inmueble del propietario. Devuelve true si se actualizó. */
    public boolean actualizar(int idPropietario, int idInmueble, String direccion, double areaMts,
                              int habitaciones, int capacidad, int idCiudad, double precioNoche) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                // El precio está en propietario_inmueble: si no se actualiza ninguna fila, el inmueble no es suyo
                int filas;
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE propietario_inmueble SET precio_noche = ? WHERE id_propietario = ? AND id_inmueble = ?")) {
                    ps.setDouble(1, precioNoche);
                    ps.setInt(2, idPropietario);
                    ps.setInt(3, idInmueble);
                    filas = ps.executeUpdate();
                }
                if (filas == 0) {
                    con.rollback();
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE inmueble SET direccion = ?, area_mts = ?, numero_habitaciones = ?, capacidad = ?, id_ciudad = ? "
                      + "WHERE id = ?")) {
                    ps.setString(1, direccion);
                    ps.setDouble(2, areaMts);
                    ps.setInt(3, habitaciones);
                    ps.setInt(4, capacidad);
                    ps.setInt(5, idCiudad);
                    ps.setInt(6, idInmueble);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** true si el inmueble tiene rentas activas que todavía no terminan. */
    public boolean tieneRentasVigentes(int idInmueble) throws SQLException {
        String sql = "SELECT COUNT(*) FROM renta WHERE id_inmueble = ? AND estado = 'activo' AND fecha_salida > CURRENT_DATE";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInmueble);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * El propietario retira uno de sus inmuebles: queda en estado 'retirado'.
     * Se conserva la asociación con el propietario y el historial de rentas.
     */
    public boolean retirar(int idPropietario, int idInmueble) throws SQLException {
        String sql = "UPDATE inmueble SET estado_aprobacion = ? "
                   + "WHERE id = ? AND id IN (SELECT id_inmueble FROM propietario_inmueble WHERE id_propietario = ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Inmueble.RETIRADO);
            ps.setInt(2, idInmueble);
            ps.setInt(3, idPropietario);
            return ps.executeUpdate() > 0;
        }
    }

    // ======================= ADMINISTRADOR =======================

    /**
     * Inmuebles que coinciden con un texto (dirección, ciudad o propietario). Máximo 100.
     * estado: "pendiente", "aprobada", "rechazado", "retirado", o vacío para traer todos.
     */
    public List<Inmueble> buscarComoAdmin(String texto, String estado) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE (i.direccion ILIKE ? OR c.nombre ILIKE ? OR u.nombre ILIKE ?) "
                   + "  AND (? = '' OR i.estado_aprobacion = ?) "        // filtro opcional por estado
                   + "ORDER BY i.id DESC LIMIT 100";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ps.setString(4, estado);
            ps.setString(5, estado);
            return leerLista(ps);
        }
    }

    /** Un inmueble por id en cualquier estado (null si no existe). */
    public Inmueble buscarComoAdminPorId(int idInmueble) throws SQLException {
        String sql = SELECT_BASE + "WHERE i.id = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInmueble);
            List<Inmueble> lista = leerLista(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    /** Cambia el estado de aprobación. Devuelve true si lo cambió. */
    public boolean cambiarEstadoAprobacion(int idInmueble, String nuevoEstado) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("UPDATE inmueble SET estado_aprobacion = ? WHERE id = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idInmueble);
            return ps.executeUpdate() > 0;
        }
    }

    /** El administrador retira un inmueble de la plataforma (queda en estado 'retirado'). */
    public boolean retirarComoAdmin(int idInmueble) throws SQLException {
        return cambiarEstadoAprobacion(idInmueble, Inmueble.RETIRADO);
    }

    // ======================= APOYO =======================

    /** Ejecuta la consulta y convierte cada fila en un objeto Inmueble. */
    private List<Inmueble> leerLista(PreparedStatement ps) throws SQLException {
        List<Inmueble> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Inmueble i = new Inmueble(
                        rs.getInt("id"),
                        rs.getString("direccion"),
                        rs.getDouble("area_mts"),
                        rs.getInt("numero_habitaciones"),
                        rs.getInt("capacidad"),
                        rs.getString("ciudad"),
                        rs.getDouble("precio_noche"),
                        rs.getString("propietario"));
                i.setEstadoAprobacion(rs.getString("estado_aprobacion"));
                i.setIdPropietario(rs.getInt("id_propietario"));
                i.setEmailPropietario(rs.getString("email_propietario"));
                lista.add(i);
            }
        }
        return lista;
    }
}
