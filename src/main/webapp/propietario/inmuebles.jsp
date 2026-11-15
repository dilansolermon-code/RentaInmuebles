<%-- CU-06 CONSULTAR INMUEBLES PROPIOS (con opción "Ver reservas") y acciones del CU-05 --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList, java.util.Locale, java.time.format.DateTimeFormatter,
                 com.rentainmuebles.datos.ReporteDAO,
                 com.rentainmuebles.modelo.Inmueble, com.rentainmuebles.modelo.Renta,
                 com.rentainmuebles.modelo.Usuario, com.rentainmuebles.modelo.Propietario,
                 com.rentainmuebles.util.Html" %>
<%
    // 1. Solo propietarios con sesión; se identifica su ID (paso 2 del diagrama de secuencia)
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("propietario")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    Propietario propietario = (Propietario) u;
    String titulo = "Mis inmuebles";
    String rol = "propietario";

    // 2. ¿Pidió "Ver reservas" de un inmueble? inmuebles.jsp?ver=5
    int ver = 0;
    try { ver = Integer.parseInt(request.getParameter("ver")); } catch (Exception e) { }

    // 3. Consultar inmuebles, reservas e indicadores
    List<Inmueble> inmuebles = new ArrayList<>();
    List<Renta> rentas = new ArrayList<>();
    Inmueble inmuebleVer = null;
    long rentasActivas = 0;
    long ingresos = 0;
    String errorBD = null;
    try {
        inmuebles = propietario.consultarInmueblesPropios();
        if (ver > 0) {
            inmuebleVer = propietario.buscarInmueblePropio(ver);
            if (inmuebleVer != null) {
                rentas = propietario.consultarReservas(ver);
            }
        }
        ReporteDAO reporte = new ReporteDAO();
        rentasActivas = reporte.rentasActivasDePropietario(u.getId());
        ingresos = reporte.ingresosDePropietario(u.getId());
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String error = request.getParameter("error");
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (request.getParameter("registrado") != null) { %>
    <div class="alerta alerta-ok">Inmueble registrado. Ya aparece en las búsquedas.</div>
<% } %>
<% if (request.getParameter("actualizado") != null) { %>
    <div class="alerta alerta-ok">Los datos del inmueble se actualizaron.</div>
<% } %>
<% if (request.getParameter("retirado") != null) { %>
    <div class="alerta alerta-aviso">Inmueble retirado y oculto de búsquedas. Su historial de rentas se conserva.</div>
<% } %>
<% if ("vigentes".equals(error)) { %>
    <div class="alerta alerta-aviso">No es posible retirar el inmueble porque tiene reservas activas.</div>
<% } else if (error != null) { %>
    <div class="alerta alerta-aviso">No se pudo completar la acción. Intenta de nuevo.</div>
<% } %>
<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
<% } %>

<div class="titulo-seccion">
    <div>
        <h1>Mis inmuebles</h1>
        <p class="suave">Administra los inmuebles que tienes publicados para renta.</p>
    </div>
    <a class="btn" href="registrar.jsp">+ Registrar inmueble</a>
</div>

<%-- Indicadores --%>
<div class="grid grid-3" style="margin-bottom:1.25rem">
    <div class="tarjeta kpi"><div class="valor"><%= inmuebles.size() %></div><div class="etiqueta">Inmuebles publicados</div></div>
    <div class="tarjeta kpi"><div class="valor"><%= rentasActivas %></div><div class="etiqueta">Reservas activas</div></div>
    <div class="tarjeta kpi"><div class="valor">$<%= String.format(Locale.US, "%,d", ingresos).replace(',', '.') %></div><div class="etiqueta">Ingresos por reservas activas</div></div>
</div>

<%-- Listado de inmuebles con datos --%>
<div class="tarjeta tabla-envoltura" style="padding:0">
    <table>
        <thead>
            <tr><th>Id</th><th>Dirección</th><th>Ciudad</th><th>Hab.</th><th>Capacidad</th><th>Precio/noche</th><th>Acciones</th></tr>
        </thead>
        <tbody>
        <% if (inmuebles.isEmpty()) { %>
            <tr><td colspan="7" class="suave">Aún no tienes inmuebles registrados.</td></tr>
        <% } %>
        <% for (Inmueble i : inmuebles) { %>
            <tr>
                <td><%= i.getId() %></td>
                <td><%= Html.esc(i.getDireccion()) %></td>
                <td><%= Html.esc(i.getCiudad()) %></td>
                <td><%= i.getNumeroHabitaciones() %></td>
                <td><%= i.getCapacidad() %></td>
                <td>$<%= String.format(Locale.US, "%,.0f", i.getPrecioNoche()).replace(',', '.') %></td>
                <td>
                    <a class="btn btn-peq" href="inmuebles.jsp?ver=<%= i.getId() %>#reservas">Ver reservas</a>
                    <a class="btn btn-sec btn-peq" href="registrar.jsp?id=<%= i.getId() %>">Editar</a>
                    <%-- Retirar pide confirmación (CU-05: "Solicita confirmación" / "Confirma retiro") --%>
                    <form action="<%= ctx %>/propietario/inmueble" method="post" style="display:inline"
                          onsubmit="return confirm('¿Seguro que deseas retirar este inmueble? Dejará de mostrarse en las búsquedas.')">
                        <input type="hidden" name="accion" value="retirar">
                        <input type="hidden" name="idInmueble" value="<%= i.getId() %>">
                        <button class="btn btn-peligro btn-peq" type="submit">Retirar</button>
                    </form>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%-- Reservas del inmueble elegido (opt "Ver reservas de un inmueble") --%>
<% if (inmuebleVer != null) { %>
<h2 id="reservas" style="margin-top:2rem">Reservas de: <%= Html.esc(inmuebleVer.getDireccion()) %></h2>
<div class="tarjeta tabla-envoltura" style="padding:0">
    <table>
        <thead>
            <tr><th>Código</th><th>Cliente</th><th>Entrada</th><th>Salida</th><th>Valor</th><th>Estado</th></tr>
        </thead>
        <tbody>
        <% if (rentas.isEmpty()) { %>
            <tr><td colspan="6" class="suave">Este inmueble aún no tiene reservas.</td></tr>
        <% } %>
        <% for (Renta r : rentas) {
               String estado = r.getEstadoTexto();
               String clase = estado.equals("Pagada") ? "ok" : (estado.equals("Cancelada") ? "mal" : "neutro");
        %>
            <tr>
                <td>R-<%= r.getId() %></td>
                <td><%= Html.esc(r.getCliente()) %></td>
                <td><%= r.getFechaEntrada().format(formato) %></td>
                <td><%= r.getFechaSalida().format(formato) %></td>
                <td>$<%= String.format(Locale.US, "%,.0f", r.getValor()).replace(',', '.') %></td>
                <td><span class="insignia <%= clase %>"><%= estado %></span></td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>
<p><a href="inmuebles.jsp">Ocultar reservas</a></p>
<% } %>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
