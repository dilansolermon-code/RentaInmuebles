package com.rentainmuebles.datos;

import com.rentainmuebles.modelo.Banco;
import com.rentainmuebles.modelo.Ciudad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Listas que se muestran en los formularios y en el inicio: ciudades y bancos. */
public class CatalogoDAO {

    /** Nombres de todas las ciudades, en orden alfabético. */
    public List<String> listarCiudades() throws SQLException {
        List<String> ciudades = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT nombre FROM ciudad ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ciudades.add(rs.getString("nombre"));
            }
        }
        return ciudades;
    }

    /** Ciudades con su id (para los formularios que guardan id_ciudad). */
    public List<Ciudad> listarCiudadesConId() throws SQLException {
        List<Ciudad> ciudades = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT id, nombre FROM ciudad ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ciudades.add(new Ciudad(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return ciudades;
    }

    /**
     * Ciudades que tienen inmuebles publicados, con cuántos tiene cada una,
     * de la que más tiene a la que menos. Ej: {Bogotá=12, Medellín=8, ...}
     * "Publicado" = el inmueble tiene fila en propietario_inmueble.
     */
    public Map<String, Long> contarInmueblesPorCiudad() throws SQLException {
        String sql = "SELECT c.nombre, COUNT(DISTINCT i.id) AS total "
                   + "FROM ciudad c "
                   + "JOIN inmueble i ON i.id_ciudad = c.id "
                   + "JOIN propietario_inmueble pi ON pi.id_inmueble = i.id "
                   + "GROUP BY c.nombre "
                   + "ORDER BY total DESC, c.nombre";
        Map<String, Long> conteo = new LinkedHashMap<>();         // guarda el orden de la consulta
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteo.put(rs.getString("nombre"), rs.getLong("total"));
            }
        }
        return conteo;
    }

    /** Todos los bancos, en orden alfabético. */
    public List<Banco> listarBancos() throws SQLException {
        List<Banco> bancos = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement("SELECT id, nombre FROM banco ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bancos.add(new Banco(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return bancos;
    }
}
