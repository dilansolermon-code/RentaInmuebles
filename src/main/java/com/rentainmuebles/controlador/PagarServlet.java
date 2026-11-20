package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Banco;
import com.rentainmuebles.modelo.Cliente;
import com.rentainmuebles.modelo.Inmueble;
import com.rentainmuebles.modelo.Renta;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * CU-03 Realizar reserva: recibe la confirmación de pago.jsp.
 * Sigue el diagrama de secuencia: valida, verifica disponibilidad y llama a Cliente.reservar(),
 * que calcula el valor, vincula el pago con el Banco, registra la renta y abona al propietario.
 */
@WebServlet("/cliente/pagar")
public class PagarServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String inicio = request.getContextPath();

        // 1. Solo un cliente con sesión puede reservar
        Cliente cliente = (Cliente) Validar.usuarioConRol(request, "cliente");
        if (cliente == null) {
            response.sendRedirect(inicio + "/login.jsp?error=sesion");
            return;
        }

        // 2. Leer el formulario
        int idInmueble = Validar.entero(request.getParameter("idInmueble"));
        int idBanco    = Validar.entero(request.getParameter("idBanco"));
        String nombreBanco = request.getParameter("nombreBanco");
        String numeroCta   = request.getParameter("numeroCta");
        String tipoCta     = request.getParameter("tipoCta");
        String entradaTxt  = request.getParameter("entrada");
        String salidaTxt   = request.getParameter("salida");

        // Dirección para volver al formulario si hay un error
        String volver = inicio + "/cliente/pago.jsp?idInmueble=" + idInmueble
                      + "&entrada=" + entradaTxt + "&salida=" + salidaTxt + "&error=";

        // 3. Validar la cuenta bancaria
        if (!Validar.cuenta(idBanco, numeroCta, tipoCta)) {
            response.sendRedirect(volver + "cuenta");
            return;
        }

        // 4. Validar las fechas
        LocalDate entrada;
        LocalDate salida;
        try {
            entrada = LocalDate.parse(entradaTxt);
            salida = LocalDate.parse(salidaTxt);
        } catch (Exception e) {
            response.sendRedirect(volver + "fechas");
            return;
        }
        if (ChronoUnit.DAYS.between(entrada, salida) < 1 || entrada.isBefore(LocalDate.now())) {
            response.sendRedirect(volver + "fechas");
            return;
        }

        try {
            Inmueble inmueble = Inmueble.buscarPublicado(idInmueble);
            if (inmueble == null) {
                response.sendRedirect(volver + "fechas");
                return;
            }
            // 5. Se verifica de nuevo justo antes de pagar: otra persona pudo reservar mientras tanto
            if (!inmueble.estaDisponible(entrada, salida)) {
                response.sendRedirect(volver + "ocupado");
                return;
            }
            // 6. El cliente reserva: calcula valor, cobra, registra la renta y abona al propietario
            Banco banco = new Banco(idBanco, nombreBanco, numeroCta, tipoCta);
            Renta renta = cliente.reservar(inmueble, entrada, salida, banco);
            if (renta == null) {
                response.sendRedirect(volver + "pago");
                return;
            }
            response.sendRedirect(inicio + "/cliente/reservas.jsp?pagada=" + renta.getId());

        } catch (SQLException e) {
            getServletContext().log("Error al registrar la reserva", e);
            if ("23505".equals(e.getSQLState())) {                  // número de cuenta repetido
                response.sendRedirect(volver + "cuentaRepetida");
                return;
            }
            response.sendRedirect(volver + "bd");
        }
    }
}
