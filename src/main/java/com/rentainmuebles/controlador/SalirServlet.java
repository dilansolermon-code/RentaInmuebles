package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Usuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Cierra la sesión (Usuario.cerrarSesion) y vuelve al inicio. */
@WebServlet("/salir")
public class SalirServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);   // false = no crear una si no existe
        if (sesion != null) {
            Usuario u = (Usuario) sesion.getAttribute("usuario");
            if (u != null) {
                u.cerrarSesion();                          // método del diagrama de clases
            }
            sesion.invalidate();                           // borra los datos de la sesión
        }
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}
