package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Administrador;
import com.rentainmuebles.modelo.Usuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * CU-08: el administrador suspende o restaura una cuenta (Administrador.suspenderCuenta).
 * Recibe: idUsuario y estado ("activo" o "inactivo").
 */
@WebServlet("/admin/estado-cuenta")
public class EstadoCuentaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String inicio = request.getContextPath();
        Administrador admin = (Administrador) Validar.usuarioConRol(request, "admin");
        if (admin == null) {
            response.sendRedirect(inicio + "/login.jsp?error=sesion");
            return;
        }

        int idUsuario = Validar.entero(request.getParameter("idUsuario"));
        String estado = request.getParameter("estado");
        String volver = inicio + "/admin/usuarios.jsp?";

        if (!Usuario.ACTIVO.equals(estado) && !Usuario.INACTIVO.equals(estado)) {
            response.sendRedirect(volver + "error=estado");
            return;
        }
        if (idUsuario == admin.getId()) {                      // no puede suspender su propia cuenta
            response.sendRedirect(volver + "error=propia");
            return;
        }
        try {
            boolean ok = admin.suspenderCuenta(idUsuario, estado);
            response.sendRedirect(volver + (ok ? "cambiado=" + estado : "error=estado"));
        } catch (SQLException e) {
            getServletContext().log("Error al cambiar estado de cuenta", e);
            response.sendRedirect(volver + "error=bd");
        }
    }
}
