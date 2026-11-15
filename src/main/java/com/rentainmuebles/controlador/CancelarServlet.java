package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Cliente;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** «extend» Cancelar reserva: anula la renta, libera las fechas y la deja inactiva. */
@WebServlet("/cliente/cancelar")
public class CancelarServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String inicio = request.getContextPath();
        Cliente cliente = (Cliente) Validar.usuarioConRol(request, "cliente");
        if (cliente == null) {
            response.sendRedirect(inicio + "/login.jsp?error=sesion");
            return;
        }

        try {
            int idRenta = Integer.parseInt(request.getParameter("idRenta"));
            boolean cancelada = cliente.cancelarReserva(idRenta);
            response.sendRedirect(inicio + "/cliente/reservas.jsp?" + (cancelada ? "cancelada=1" : "error=cancelar"));
        } catch (NumberFormatException | SQLException e) {
            getServletContext().log("Error al cancelar la renta", e);
            response.sendRedirect(inicio + "/cliente/reservas.jsp?error=cancelar");
        }
    }
}
