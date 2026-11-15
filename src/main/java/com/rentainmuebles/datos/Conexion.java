package com.rentainmuebles.datos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Abre la conexión con PostgreSQL usando los datos de db.properties. */
public class Conexion {

    public static Connection obtener() throws SQLException {
        Properties datos = new Properties();                    // guarda pares nombre=valor
        // Busca db.properties dentro del proyecto (carpeta resources)
        try (InputStream archivo = Conexion.class.getResourceAsStream("/db.properties")) {
            if (archivo == null) {                              // la persona no ha creado el archivo
                throw new SQLException("Falta src/main/resources/db.properties: copia db.properties.ejemplo");
            }
            datos.load(archivo);                                // lee url, usuario y clave
        } catch (IOException e) {
            throw new SQLException("No se pudo leer db.properties", e);
        }
        try {
            Class.forName("org.postgresql.Driver");             // carga el driver de PostgreSQL
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de PostgreSQL. Haz Clean and Build.", e);
        }
        return DriverManager.getConnection(datos.getProperty("url"),      // abre la conexión
                datos.getProperty("usuario"), datos.getProperty("clave"));
    }
}