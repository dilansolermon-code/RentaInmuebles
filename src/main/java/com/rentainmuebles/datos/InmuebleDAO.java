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
 * Un inmueble está PUBLICADO cuando tiene su registro en propietario_inmueble
 * (relación Asocia). Retirarlo borra solo ese registro: el inmueble y sus rentas se conservan.
 */
public class InmuebleDAO {

    // Parte común de las consultas: solo inmuebles publicados (JOIN con propietario_inmueble)
    private static final String SELECT_BASE =
          "SELECT i.id, i.direccion, i.area_mts, i.numero_habitaciones, i.capacidad, "
        + "       c.nombre AS ciudad, pi.precio_noche, u.nombre AS propietario "
        + "FROM inmueble i "
        + "JOIN ciudad c ON c.id = i.id_ciudad "
        + "JOIN propietario_inmueble pi ON pi.id_inmueble = i.id "
        + "LEFT JOIN usuario u ON u.id = pi.id_propietario ";

    // ======================= CLIENTE =======================

    /**
     * Lista los inmuebles de una ciudad con capacidad para cierto número de personas.
     * Si ciudad está vacía, trae todas las ciudades.
     */
    public List<Inmueble> buscar(String ciudad, int personas) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE (? = '' OR c.nombre = ?) AND i.capacidad >= ? "
                   + "ORDER BY i.id";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ciudad);
            ps.setString(2, ciudad);
            ps.setInt(3, personas);
            return leerLista(ps);
        }
    }

    /** Busca un inmueble publicado por su id. Devuelve null si no existe. */
    public Inmueble buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE i.id = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            List<Inmueble> lista = leerLista(ps);
            return lista.isEmpty() ? null : lista.get(0);
        }
    }

    // ======================= PROPIETARIO =======================

    /** Inmuebles publicados por un propietario. */
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

    /** Registra un inmueble y lo publica (inmueble + propietario_inmueble) en una transacción. */
    public int registrar(int idPropietario, String direccion, double areaMts, int habitaciones,
                         int capacidad, int idCiudad, double precioNoche) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int idInmueble;
                String sqlInmueble = "INSERT INTO inmueble (id, direccion, area_mts, numero_habitaciones, capacidad, id_ciudad) "
                                   + "VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM inmueble), ?, ?, ?, ?, ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(sqlInmueble)) {
                    ps.setString(1, direccion);
                    ps.setDouble(2, areaMts);
                    ps.setInt(3, habitaciones);
                    ps.setInt(4, capacidad);
                    ps.setInt(5, idCiudad);
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

    /** El propietario retira (deja de publicar) uno de sus inmuebles. */
    public boolean retirar(int idPropietario, int idInmueble) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM propietario_inmueble WHERE id_propietario = ? AND id_inmueble = ?")) {
            ps.setInt(1, idPropietario);
            ps.setInt(2, idInmueble);
            return ps.executeUpdate() > 0;
        }
    }

    // ======================= ADMINISTRADOR =======================

    /** Inmuebles publicados que coinciden con un texto (dirección, ciudad o propietario). Máximo 100. */
    public List<Inmueble> buscarComoAdmin(String texto) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE i.direccion ILIKE ? OR c.nombre ILIKE ? OR u.nombre ILIKE ? "
                   + "ORDER BY i.id DESC LIMIT 100";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            return leerLista(ps);
        }
    }

    /** El administrador retira un inmueble de la plataforma (sin importar el propietario). */
    public boolean retirarComoAdmin(int idInmueble) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("DELETE FROM propietario_inmueble WHERE id_inmueble = ?")) {
            ps.setInt(1, idInmueble);
            return ps.executeUpdate() > 0;
        }
    }

    // ======================= APOYO =======================

    /** Ejecuta la consulta y convierte cada fila en un objeto Inmueble. */
    private List<Inmueble> leerLista(PreparedStatement ps) throws SQLException {
        List<Inmueble> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Inmueble(
                        rs.getInt("id"),
                        rs.getString("direccion"),
                        rs.getDouble("area_mts"),
                        rs.getInt("numero_habitaciones"),
                        rs.getInt("capacidad"),
                        rs.getString("ciudad"),
                        rs.getDouble("precio_noche"),
                        rs.getString("propietario")));
            }
        }
        return lista;
    }
}
