<%-- CU-09 CONSULTAR REPORTES: tipo de reporte y rango de fechas, como en los diagramas de actividades y secuencia --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.Map, java.util.Locale, java.time.LocalDate,
                 com.rentainmuebles.modelo.Usuario, com.rentainmuebles.modelo.Administrador,
                 com.rentainmuebles.modelo.Reporte, com.rentainmuebles.util.Html" %>
<%
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("admin")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    Administrador admin = (Administrador) u;
    String titulo = "Reportes";
    String rol = "admin";

    // 1. Tipo de reporte y rango de fechas (por defecto: del último año al próximo)
    String tipo = request.getParameter("tipo");
    if (!"ingresos".equals(tipo) && !"inmuebles".equals(tipo)) {
        tipo = "ciudad";
    }
    LocalDate desde = LocalDate.now().minusYears(1);
    LocalDate hasta = LocalDate.now().plusYears(1);
    boolean fechasValidas = true;
    try {
        if (request.getParameter("desde") != null) {
            desde = LocalDate.parse(request.getParameter("desde"));
            hasta = LocalDate.parse(request.getParameter("hasta"));
        }
    } catch (Exception e) {
        fechasValidas = false;
    }
    if (desde.isAfter(hasta)) {                      // ¿Rango válido?
        fechasValidas = false;
    }

    // 2. El administrador genera el reporte: un objeto que ya trae totales y cálculos
    Reporte reporte = null;
    String errorBD = null;
    if (fechasValidas) {
        try {
            reporte = admin.consultarReportes(tipo, desde, hasta);
        } catch (Exception e) {
            errorBD = e.getMessage();
        }
    }
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (!fechasValidas) { %>
    <div class="alerta alerta-aviso">La fecha inicial debe ser anterior a la fecha final.</div>
<% } %>
<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
<% } %>

<div class="titulo-seccion">
    <div>
        <h1>Reportes</h1>
        <p class="suave">Elige el tipo de reporte y el periodo (según la fecha de entrada de las rentas).</p>
    </div>
</div>

<%-- Formulario: tipo de reporte + rango de fechas + Generar --%>
<form class="tarjeta" action="reportes.jsp" method="get"
      style="display:grid;grid-template-columns:2fr 1fr 1fr auto;gap:.75rem;align-items:end;margin-bottom:1.25rem">
    <div class="campo">
        <label for="tipo">Tipo de reporte</label>
        <select id="tipo" name="tipo">
            <option value="ciudad"    <%= tipo.equals("ciudad") ? "selected" : "" %>>Rentas activas por ciudad</option>
            <option value="ingresos"  <%= tipo.equals("ingresos") ? "selected" : "" %>>Ingresos por ciudad</option>
            <option value="inmuebles" <%= tipo.equals("inmuebles") ? "selected" : "" %>>Inmuebles más rentados</option>
        </select>
    </div>
    <div class="campo">
        <label for="desde">Desde</label>
        <input type="date" id="desde" name="desde" value="<%= desde %>" required>
    </div>
    <div class="campo">
        <label for="hasta">Hasta</label>
        <input type="date" id="hasta" name="hasta" value="<%= hasta %>" required>
    </div>
    <button class="btn" type="submit">Generar</button>
</form>

<% if (reporte != null) {
       boolean esDinero = reporte.esDinero();
%>
<div class="grid grid-4">
    <div class="tarjeta kpi"><div class="valor"><%= reporte.getRentasActivas() %></div><div class="etiqueta">Rentas activas del periodo</div></div>
    <div class="tarjeta kpi"><div class="valor">$<%= String.format(Locale.US, "%,d", reporte.getValorRentasActivas()).replace(',', '.') %></div><div class="etiqueta">Valor de rentas activas (COP)</div></div>
    <div class="tarjeta kpi"><div class="valor"><%= reporte.getRentasCanceladas() %></div><div class="etiqueta">Rentas canceladas del periodo</div></div>
    <div class="tarjeta kpi"><div class="valor"><%= reporte.getInmueblesPublicados() %> / <%= reporte.getTotalUsuarios() %></div><div class="etiqueta">Inmuebles publicados / usuarios</div></div>
</div>

<div class="tarjeta" style="margin-top:1.25rem">
    <h3><%= reporte.getTitulo() %></h3>
    <% if (reporte.estaVacio()) { %>
        <p class="suave">No hay información para el periodo seleccionado.</p>
    <% } else { %>
    <table>
        <thead><tr><th><%= tipo.equals("inmuebles") ? "Inmueble" : "Ciudad" %></th><th></th><th><%= esDinero ? "Valor (COP)" : "Rentas" %></th></tr></thead>
        <% for (Map.Entry<String, Long> fila : reporte.getDatos().entrySet()) { %>
            <tr>
                <td><%= Html.esc(fila.getKey()) %></td>
                <td style="width:45%"><div class="barra-dato"><div style="width:<%= reporte.porcentaje(fila.getValue()) %>%"></div></div></td>
                <td><%= esDinero ? "$" + String.format(Locale.US, "%,d", fila.getValue()).replace(',', '.') : fila.getValue() %></td>
            </tr>
        <% } %>
        <tr>
            <td><strong>Total</strong></td><td></td>
            <td><strong><%= esDinero ? "$" + String.format(Locale.US, "%,d", reporte.getTotal()).replace(',', '.') : reporte.getTotal() %></strong></td>
        </tr>
    </table>
    <% } %>
</div>
<% } %>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
