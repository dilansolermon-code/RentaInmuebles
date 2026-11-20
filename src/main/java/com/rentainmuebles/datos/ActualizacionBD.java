package com.rentainmuebles.datos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Ajustes que la aplicación le hace a la base de datos al iniciar, para que nadie
 * tenga que ejecutar scripts a mano. Cada ajuste revisa primero si ya está hecho,
 * así que se puede ejecutar todas las veces que arranque la aplicación sin dañar nada.
 */
public class ActualizacionBD {

    /**
     * Permite el estado 'retirado' en inmueble.estado_aprobacion (CU-05 y CU-06).
     * La base original solo acepta pendiente, aprobada y rechazado.
     * Devuelve true si hizo el cambio y false si la base ya lo tenía.
     */
    public static boolean permitirEstadoRetirado() throws SQLException {
        // Reglas CHECK de la tabla inmueble con su definición, ej: CHECK (estado_aprobacion IN (...))
        String consulta = "SELECT conname, pg_get_constraintdef(oid) FROM pg_constraint "
                        + "WHERE conrelid = 'inmueble'::regclass AND contype = 'c'";

        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {

            // 1. Buscar las reglas que hablan de estado_aprobacion
            List<String> reglasViejas = new ArrayList<>();
            try (ResultSet rs = st.executeQuery(consulta)) {
                while (rs.next()) {
                    String definicion = rs.getString(2);
                    if (definicion.contains("estado_aprobacion")) {
                        if (definicion.contains("retirado")) {
                            return false;                       // ya acepta 'retirado': no hay nada que hacer
                        }
                        reglasViejas.add(rs.getString(1));      // nombre de la regla que hay que cambiar
                    }
                }
            }

            // 2. Cambiar la regla en una sola transacción (todo o nada)
            con.setAutoCommit(false);
            try {
                for (String nombre : reglasViejas) {
                    st.executeUpdate("ALTER TABLE inmueble DROP CONSTRAINT \"" + nombre + "\"");
                }
                st.executeUpdate("ALTER TABLE inmueble ADD CONSTRAINT inmueble_estado_aprobacion_check "
                               + "CHECK (estado_aprobacion IN ('pendiente', 'aprobada', 'rechazado', 'retirado'))");
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();                                 // si algo falla, la base queda como estaba
                throw e;
            }
        }
    }
}
