package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.CatalogoDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Una ciudad del catálogo (tabla ciudad). */
public class Ciudad {

    private int id;
    private String nombre;

    public Ciudad(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /** Todas las ciudades con su id (para los formularios que guardan id_ciudad). */
    public static List<Ciudad> listarTodas() throws SQLException {
        return new CatalogoDAO().listarCiudadesConId();
    }

    /** Solo los nombres de las ciudades (para el filtro de búsqueda). */
    public static List<String> listarNombres() throws SQLException {
        return new CatalogoDAO().listarCiudades();
    }

    /** Ciudades con inmuebles aprobados y cuántos tiene cada una, de mayor a menor (página de inicio). */
    public static Map<String, Long> contarInmueblesAprobados() throws SQLException {
        return new CatalogoDAO().contarInmueblesPorCiudad();
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
}
