package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Usuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Mi perfil: actualiza datos de contacto y cuenta bancaria (cliente o propietario). */
@WebServlet("/perfil")
public class PerfilServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String inicio = request.getContextPath();

        Usuario u = (Usuario) request.getSession().getAttribute("usuario");
        if (u == null || u.getRol().equals("admin")) {               // el admin no tiene cuenta bancaria
            response.sendRedirect(inicio + "/login.jsp?error=sesion");
            return;
        }

        String nombre    = texto(request.getParameter("nombre"));
        String telefono  = texto(request.getParameter("telefono"));
        String email     = texto(request.getParameter("email")).toLowerCase();
        String numeroCta = texto(request.getParameter("numeroCta"));
        String tipoCta   = request.getParameter("tipoCta");
        int idBanco = Validar.entero(request.getParameter("idBanco"));

        // Si deja el número vacío, se conserva el que tenía (se envía null)
        String numeroNuevo = numeroCta.isEmpty() ? null : numeroCta;
        boolean cuentaValida = numeroNuevo == null
                ? idBanco > 0 && ("Ahorros".equals(tipoCta) || "Corriente".equals(tipoCta))
                : Validar.cuenta(idBanco, numeroNuevo, tipoCta);

        if (nombre.isEmpty() || !email.contains("@") || !cuentaValida) {
            response.sendRedirect(inicio + "/perfil.jsp?error=datos");
            return;
        }

        try {
            // Si cambió el email, no puede ser uno que ya use otra persona
            if (!email.equals(u.getEmail()) && Usuario.emailExiste(email)) {
                response.sendRedirect(inicio + "/perfil.jsp?error=email");
                return;
            }
            // El mismo objeto de la sesión se actualiza a sí mismo (base de datos y atributos)
            u.actualizarPerfil(nombre, telefono, email, idBanco, numeroNuevo, tipoCta);
            response.sendRedirect(inicio + "/perfil.jsp?guardado=1");
        } catch (SQLException e) {
            getServletContext().log("Error al actualizar el perfil", e);
            if ("23505".equals(e.getSQLState())) {                  // número de cuenta repetido
                response.sendRedirect(inicio + "/perfil.jsp?error=cuentaRepetida");
                return;
            }
            response.sendRedirect(inicio + "/perfil.jsp?error=bd");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
