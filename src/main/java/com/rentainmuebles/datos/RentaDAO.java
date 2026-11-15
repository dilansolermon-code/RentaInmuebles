package com.rentainmuebles.datos;

import com.rentainmuebles.modelo.Renta;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla renta (reservas) y a la cuenta bancaria del cliente. */
public class RentaDAO {

    // Consulta común: la renta con la dirección y ciudad del inmueble y el banco del cliente
    private static final String SELECT_BASE =
          "SELECT r.id, r.fecha_entrada, r.fecha_salida, r.valor, r.estado, r.id_inmueble, "
        + "       i.direccion, c.nombre AS ciudad, "
        + "       (SELECT b.nombre FROM cuenta_cliente cc JOIN banco b ON b.id = cc.id_banco "
        + "         WHERE cc.id_cliente = r.id_cliente ORDER BY cc.id DESC LIMIT 1) AS banco "
        + "FROM renta r "
        + "JOIN inmueble i ON i.id = r.id_inmueble "
        + "JOIN ciudad c ON c.id = i.id_ciudad ";

    /** Todas las rentas de un cliente, de la más reciente a la más antigua. */
    public List<Renta> listarPorCliente(int idCliente) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.id_cliente = ? ORDER BY r.fecha_entrada DESC";
        List<Renta> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearRenta(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Rentas de los inmuebles de un propietario, con el nombre del cliente (máximo 100).
     * Si idInmueble es mayor que 0, solo las de ese inmueble (CU-06 «Ver reservas»).
     */
    public List<Renta> listarPorPropietario(int idPropietario, int idInmueble) throws SQLException {
        String sql = "SELECT r.id, r.fecha_entrada, r.fecha_salida, r.valor, r.estado, r.id_inmueble, "
                   + "       i.direccion, c.nombre AS ciudad, NULL AS banco, uc.nombre AS cliente "
                   + "FROM renta r "
                   + "JOIN inmueble i ON i.id = r.id_inmueble "
                   + "JOIN ciudad c ON c.id = i.id_ciudad "
                   + "JOIN propietario_inmueble pi ON pi.id_inmueble = r.id_inmueble "
                   + "JOIN usuario uc ON uc.id = r.id_cliente "
                   + "WHERE pi.id_propietario = ? AND (? = 0 OR r.id_inmueble = ?) "
                   + "ORDER BY r.fecha_entrada DESC LIMIT 100";
        List<Renta> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPropietario);
            ps.setInt(2, idInmueble);
            ps.setInt(3, idInmueble);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Renta r = crearRenta(rs);
                    r.setCliente(rs.getString("cliente"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /**
     * Verifica que el inmueble no tenga otra renta activa que se cruce con esas fechas.
     * Dos rangos se cruzan si uno empieza antes de que el otro termine.
     */
    public boolean estaDisponible(int idInmueble, LocalDate entrada, LocalDate salida) throws SQLException {
        String sql = "SELECT COUNT(*) FROM renta "
                   + "WHERE id_inmueble = ? AND estado = 'activo' "
                   + "AND fecha_entrada < ? AND fecha_salida > ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInmueble);
            ps.setDate(2, Date.valueOf(salida));    // la otra renta empieza antes de mi salida
            ps.setDate(3, Date.valueOf(entrada));   // y termina después de mi entrada
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;           // 0 cruces = disponible
            }
        }
    }

    /**
     * Realiza el pago: guarda la cuenta bancaria del cliente y crea la renta en estado "activo".
     * Todo ocurre en una TRANSACCIÓN: o se guardan los dos cambios, o ninguno.
     * Devuelve el id de la renta creada.
     */
    public int pagar(int idCliente, int idInmueble, LocalDate entrada, LocalDate salida,
                     double valor, int idBanco, String numeroCta, String tipoCta) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);                          // inicia la transacción
            try {
                guardarCuenta(con, idCliente, idBanco, numeroCta, tipoCta);
                int idPagada = insertarRenta(con, idCliente, idInmueble, entrada, salida, valor);
                con.commit();                                  // confirma los cambios
                return idPagada;
            } catch (SQLException e) {
                con.rollback();                                // deshace todo si algo falló
                throw e;
            }
        }
    }

    /** Cancela una renta del cliente. Devuelve true si se canceló. */
    public boolean cancelar(int idRenta, int idCliente) throws SQLException {
        String sql = "UPDATE renta SET estado = 'inactivo' "
                   + "WHERE id = ? AND id_cliente = ? AND estado = 'activo'";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRenta);
            ps.setInt(2, idCliente);
            return ps.executeUpdate() == 1;                    // 1 fila modificada = éxito
        }
    }

    // ---------------- Métodos privados de apoyo ----------------

    /** Actualiza la cuenta del cliente; si no tiene, la crea. */
    private void guardarCuenta(Connection con, int idCliente, int idBanco,
                               String numeroCta, String tipoCta) throws SQLException {
        String actualizar = "UPDATE cuenta_cliente SET numero_cta = ?, tipo_cta = ?, id_banco = ? "
                          + "WHERE id_cliente = ?";
        try (PreparedStatement ps = con.prepareStatement(actualizar)) {
            ps.setString(1, numeroCta);
            ps.setString(2, tipoCta);
            ps.setInt(3, idBanco);
            ps.setInt(4, idCliente);
            if (ps.executeUpdate() > 0) {
                return;                                        // ya tenía cuenta y se actualizó
            }
        }
        // No tenía cuenta: se crea con el siguiente id disponible
        String insertar = "INSERT INTO cuenta_cliente (id, numero_cta, tipo_cta, id_banco, id_cliente) "
                        + "VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM cuenta_cliente), ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(insertar)) {
            ps.setString(1, numeroCta);
            ps.setString(2, tipoCta);
            ps.setInt(3, idBanco);
            ps.setInt(4, idCliente);
            ps.executeUpdate();
        }
    }

    /** Crea una renta nueva ya pagada (estado "activo") y devuelve su id. */
    private int insertarRenta(Connection con, int idCliente, int idInmueble,
                              LocalDate entrada, LocalDate salida, double valor) throws SQLException {
        String sql = "INSERT INTO renta (id, fecha_entrada, fecha_salida, valor, estado, id_cliente, id_inmueble) "
                   + "VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM renta), ?, ?, ?, 'activo', ?, ?) "
                   + "RETURNING id";                           // devuelve el id que se asignó
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(entrada));
            ps.setDate(2, Date.valueOf(salida));
            ps.setDouble(3, valor);
            ps.setInt(4, idCliente);
            ps.setInt(5, idInmueble);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Convierte la fila actual del resultado en un objeto Renta. */
    private Renta crearRenta(ResultSet rs) throws SQLException {
        return new Renta(
                rs.getInt("id"),
                rs.getDate("fecha_entrada").toLocalDate(),
                rs.getDate("fecha_salida").toLocalDate(),
                rs.getDouble("valor"),
                rs.getString("estado"),
                rs.getInt("id_inmueble"),
                rs.getString("direccion"),
                rs.getString("ciudad"),
                rs.getString("banco"));
    }
}
