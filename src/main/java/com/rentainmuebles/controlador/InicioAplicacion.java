package com.rentainmuebles.controlador;

import com.rentainmuebles.datos.ActualizacionBD;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.SQLException;

/**
 * Se ejecuta UNA vez, cuando Tomcat inicia la aplicación (@WebListener).
 * Deja la base de datos lista para que funcionen todas las opciones,
 * sin que el usuario tenga que ejecutar scripts en pgAdmin.
 */
@WebListener
public class InicioAplicacion implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try {
            if (ActualizacionBD.permitirEstadoRetirado()) {
                evento.getServletContext().log("Base de datos actualizada: se permite el estado 'retirado' en inmueble.");
            }
        } catch (SQLException e) {
            // La aplicación sigue funcionando; el mensaje queda en la ventana Output de NetBeans
            evento.getServletContext().log("No se pudo actualizar la base de datos al iniciar", e);
        }
    }
}
