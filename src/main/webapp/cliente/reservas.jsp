<%-- CU-04 CONSULTAR MIS RESERVAS: rentas del cliente guardadas en PostgreSQL --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList, java.util.Locale, java.time.format.DateTimeFormatter,
                 com.rentainmuebles.modelo.Renta, com.rentainmuebles.modelo.Usuario, com.rentainmuebles.modelo.Cliente" %>
<%
    // 1. Solo un cliente con sesión puede ver sus reservas
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("cliente")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    String titulo = "Mis reservas";
    String rol = "cliente";

    // 2. Identifica al cliente en sesión y consulta sus rentas (Cliente.consultarReservas)
    Cliente cliente = (Cliente) u;
    List<Renta> rentas = new ArrayList<>();
    String errorBD = null;
    try {
        rentas = cliente.consultarReservas();
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (request.getParameter("pagada") != null) { %>
    <div class="alerta alerta-ok">Reserva confirmada. El pago quedó vinculado con tu banco y se abonó al propietario.</div>
<% } %>
<% if (request.getParameter("cancelada") != null) { %>
    <div class="alerta alerta-aviso">Reserva cancelada. Las fechas quedaron libres.</div>
<% } %>
<% if ("cancelar".equals(request.getParameter("error"))) { %>
    <div class="alerta alerta-aviso">No se pudo cancelar la reserva.</div>
<% } %>
<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= com.rentainmuebles.util.Html.esc(errorBD) %></div>
<% } %>

<div class="titulo-seccion">
    <div>
        <h1>Mis reservas</h1>
        <p class="suave">Consulta el estado de tus rentas y cancela las que aún no han comenzado.</p>
    </div>
    <a class="btn" href="buscar.jsp">Nueva búsqueda</a>
</div>

<div class="tarjeta tabla-envoltura" style="padding:0">
    <table>
        <thead>
            <tr>
                <th>Código</th>
                <th>Inmueble</th>
                <th>Ciudad</th>
                <th>Entrada</th>
                <th>Salida</th>
                <th>Valor</th>
                <th>Pagado con</th>
                <th>Estado</th>
                <th>Acción</th>
            </tr>
        </thead>
        <tbody>
        <% if (rentas.isEmpty() && errorBD == null) { %>
            <tr><td colspan="9" class="suave">Aún no tienes reservas realizadas. ¡Explora nuestros inmuebles!</td></tr>
        <% } %>

        <% for (Renta r : rentas) {
               // La renta sabe decir su estado en palabras: "Pagada", "Finalizada" o "Cancelada"
               String estado = r.getEstadoTexto();
               String clase = "neutro";                       // color de la insignia
               if (estado.equals("Pagada")) clase = "ok";
               if (estado.equals("Cancelada")) clase = "mal";
               boolean conPago = r.estaActiva();              // el banco solo aplica si no se canceló
               String valor = String.format(Locale.US, "%,.0f", r.getValor()).replace(',', '.');
        %>
            <tr>
                <td>R-<%= r.getId() %></td>
                <td><%= com.rentainmuebles.util.Html.esc(r.getDireccion()) %></td>
                <td><%= r.getCiudad() %></td>
                <td><%= r.getFechaEntrada().format(formato) %></td>
                <td><%= r.getFechaSalida().format(formato) %></td>
                <td>$<%= valor %></td>
                <td><%= (conPago && r.getBanco() != null) ? r.getBanco() : "—" %></td>
                <td><span class="insignia <%= clase %>"><%= estado %></span></td>
                <td>
                    <% if (r.sePuedeCancelar()) { %>
                        <%-- Cancelar cambia datos, por eso va en un formulario POST --%>
                        <form action="<%= ctx %>/cliente/cancelar" method="post" style="display:inline"
                              onsubmit="return confirm('¿Seguro que deseas cancelar esta reserva?')">
                            <input type="hidden" name="idRenta" value="<%= r.getId() %>">
                            <button class="btn btn-peligro btn-peq" type="submit">Cancelar</button>
                        </form>
                    <% } else { %>
                        <span class="suave">—</span>
                    <% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
