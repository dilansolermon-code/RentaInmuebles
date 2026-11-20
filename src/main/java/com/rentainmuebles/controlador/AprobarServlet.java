package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Administrador;
import com.rentainmuebles.modelo.Inmueble;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * CU-07: el administrador aprueba o rechaza un inmueble (Administrador.aprobarInmueble).
 * Recibe: idInmueble y estado ("aprobada" o "rechazado").
 */
@WebServlet("/admin/aprobar")
public class AprobarServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String inicio = request.getContextPath();
        Administrador admin = (Administrador) Validar.usuarioConRol(request, "admin");
        if (admin == null) {
            response.sendRedirect(inicio + "/login.jsp?error=sesion");
            return;
        }

        int idInmueble = Validar.entero(request.getParameter("idInmueble"));
        String estado = request.getParameter("estado");
        String volver = inicio + "/admin/inmuebles.jsp?";

        // Solo se aceptan los dos valores de esta acción
        if (!Inmueble.APROBADA.equals(estado) && !Inmueble.RECHAZADO.equals(estado)) {
            response.sendRedirect(volver + "error=estado");
            return;
        }
        try {
            boolean ok = admin.aprobarInmueble(idInmueble, estado);
            response.sendRedirect(volver + (ok ? "cambiado=" + estado : "error=estado"));
        } catch (SQLException e) {
            getServletContext().log("Error al aprobar inmueble", e);
            response.sendRedirect(volver + "error=bd");
        }
    }
}
