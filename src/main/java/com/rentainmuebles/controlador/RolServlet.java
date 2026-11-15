package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Usuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Recibe el rol elegido en elegir-rol.jsp ("cliente", "propietario" o "admin").
 * Solo acepta roles que ya fueron verificados con la contraseña en LoginServlet.
 */
@WebServlet("/rol")
public class RolServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String elegido = request.getParameter("rol");
        @SuppressWarnings("unchecked")
        List<Usuario> roles = (List<Usuario>) request.getSession().getAttribute("roles");

        if (roles != null) {
            for (Usuario u : roles) {
                if (u.getRol().equals(elegido)) {
                    LoginServlet.entrarComo(u, request, response);   // entra con ese rol
                    return;
                }
            }
        }
        // Sin roles verificados en la sesión: debe iniciar sesión otra vez
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
    }
}
