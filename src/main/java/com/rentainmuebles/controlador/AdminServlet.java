package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Administrador;
import com.rentainmuebles.modelo.Inmueble;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** El administrador retira un inmueble de la plataforma (queda en estado "retirado"). */
@WebServlet("/admin/retirar")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String inicio = request.getContextPath();
        Administrador admin = (Administrador) Validar.usuarioConRol(request, "admin");
        if (admin == null) {
            response.sendRedirect(inicio + "/login.jsp?error=sesion");
            return;
        }

        int idInmueble = Validar.entero(request.getParameter("idInmueble"));
        String volver = inicio + "/admin/inmuebles.jsp?";
        try {
            Inmueble inmueble = admin.buscarInmueble(idInmueble);
            if (inmueble == null) {
                response.sendRedirect(volver + "error=retirar");
                return;
            }
            if (inmueble.tieneReservasActivas()) {                // no se retira con reservas activas
                response.sendRedirect(volver + "error=vigentes");
                return;
            }
            boolean ok = admin.retirarInmueble(idInmueble);
            response.sendRedirect(volver + (ok ? "retirado=" + idInmueble : "error=retirar"));
        } catch (SQLException e) {
            getServletContext().log("Error al retirar inmueble", e);
            response.sendRedirect(volver + "error=bd");
        }
    }
}
