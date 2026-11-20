<%-- CU-07 APROBAR INMUEBLE: el administrador revisa, aprueba, rechaza o retira inmuebles --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList, java.util.Locale,
                 com.rentainmuebles.modelo.Inmueble, com.rentainmuebles.modelo.Usuario,
                 com.rentainmuebles.modelo.Administrador, com.rentainmuebles.util.Html" %>
<%
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("admin")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    Administrador admin = (Administrador) u;
    String titulo = "Inmuebles";
    String rol = "admin";

    String texto = request.getParameter("buscar") == null ? "" : request.getParameter("buscar").trim();
    // Filtro por estado. Por defecto muestra los pendientes (opción "Inmuebles pendientes" del CU-07)
    String estado = request.getParameter("estado") == null ? Inmueble.PENDIENTE : request.getParameter("estado");
    List<Inmueble> inmuebles = new ArrayList<>();
    String errorBD = null;
    try {
        inmuebles = admin.consultarInmuebles(texto, estado);
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
    String error = request.getParameter("error");
    String cambiado = request.getParameter("cambiado");
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (Inmueble.APROBADA.equals(cambiado)) { %>
    <div class="alerta alerta-ok">Inmueble aprobado correctamente.</div>
<% } else if (Inmueble.RECHAZADO.equals(cambiado)) { %>
    <div class="alerta alerta-aviso">Inmueble rechazado y no publicado.</div>
<% } %>
<% if (request.getParameter("retirado") != null) { %>
    <div class="alerta alerta-aviso">El inmueble fue retirado de la plataforma. Su historial de rentas se conserva.</div>
<% } %>
<% if ("vigentes".equals(error)) { %>
    <div class="alerta alerta-aviso">No es posible retirar el inmueble porque tiene reservas activas.</div>
<% } else if ("estado".equals(error)) { %>
    <div class="alerta alerta-aviso">No se pudo cambiar el estado del inmueble.</div>
<% } else if (error != null) { %>
    <div class="alerta alerta-aviso">No se pudo completar la acción.</div>
<% } %>
<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
<% } %>

<div class="titulo-seccion">
    <div>
        <h1><%= Inmueble.PENDIENTE.equals(estado) ? "Inmuebles pendientes" : "Inmuebles" %></h1>
        <p class="suave">Revisa los inmuebles registrados por los propietarios y apruébalos o recházalos. Se muestran hasta 100.</p>
    </div>
    <form action="inmuebles.jsp" method="get" style="display:flex;gap:.5rem">
        <select name="estado">
            <option value="<%= Inmueble.PENDIENTE %>" <%= Inmueble.PENDIENTE.equals(estado) ? "selected" : "" %>>Pendientes</option>
            <option value="<%= Inmueble.APROBADA %>"  <%= Inmueble.APROBADA.equals(estado)  ? "selected" : "" %>>Aprobados</option>
            <option value="<%= Inmueble.RECHAZADO %>" <%= Inmueble.RECHAZADO.equals(estado) ? "selected" : "" %>>Rechazados</option>
            <option value="<%= Inmueble.RETIRADO %>"  <%= Inmueble.RETIRADO.equals(estado)  ? "selected" : "" %>>Retirados</option>
            <option value=""                          <%= "".equals(estado)                 ? "selected" : "" %>>Todos</option>
        </select>
        <input name="buscar" value="<%= Html.esc(texto) %>" placeholder="Dirección, ciudad o propietario" style="min-width:240px">
        <button class="btn" type="submit">Buscar</button>
    </form>
</div>

<div class="tarjeta tabla-envoltura" style="padding:0">
    <table>
        <thead>
            <tr><th>Id</th><th>Dirección</th><th>Ciudad</th><th>Propietario</th><th>Email propietario</th><th>Capacidad</th><th>Precio/noche</th><th>Estado</th><th>Acciones</th></tr>
        </thead>
        <tbody>
        <% if (inmuebles.isEmpty() && errorBD == null) { %>
            <tr><td colspan="9" class="suave">
                <%= Inmueble.PENDIENTE.equals(estado) && texto.isEmpty()
                        ? "No hay inmuebles pendientes de aprobación."
                        : "No hay inmuebles que coincidan con la búsqueda." %>
            </td></tr>
        <% } %>
        <% for (Inmueble i : inmuebles) {
               String clase = i.estaAprobado() ? "ok" : (i.getEstadoTexto().equals("Pendiente") ? "neutro" : "mal");
        %>
            <tr>
                <td><%= i.getId() %></td>
                <td><%= Html.esc(i.getDireccion()) %></td>
                <td><%= Html.esc(i.getCiudad()) %></td>
                <td><%= Html.esc(i.getPropietario()) %></td>
                <td><%= Html.esc(i.getEmailPropietario()) %></td>
                <td><%= i.getCapacidad() %></td>
                <td>$<%= String.format(Locale.US, "%,.0f", i.getPrecioNoche()).replace(',', '.') %></td>
                <td><span class="insignia <%= clase %>"><%= i.getEstadoTexto() %></span></td>
                <td>
                    <% if (!i.estaRetirado()) { %>
                        <% if (!i.estaAprobado()) { %>
                        <form action="<%= ctx %>/admin/aprobar" method="post" style="display:inline">
                            <input type="hidden" name="idInmueble" value="<%= i.getId() %>">
                            <input type="hidden" name="estado" value="<%= Inmueble.APROBADA %>">
                            <button class="btn btn-peq" type="submit">Aprobar</button>
                        </form>
                        <% } %>
                        <% if (!Inmueble.RECHAZADO.equals(i.getEstadoAprobacion())) { %>
                        <form action="<%= ctx %>/admin/aprobar" method="post" style="display:inline"
                              onsubmit="return confirm('¿Rechazar este inmueble? No se publicará en el catálogo.')">
                            <input type="hidden" name="idInmueble" value="<%= i.getId() %>">
                            <input type="hidden" name="estado" value="<%= Inmueble.RECHAZADO %>">
                            <button class="btn btn-sec btn-peq" type="submit">Rechazar</button>
                        </form>
                        <% } %>
                        <form action="<%= ctx %>/admin/retirar" method="post" style="display:inline"
                              onsubmit="return confirm('¿Seguro que deseas retirar este inmueble de la plataforma?')">
                            <input type="hidden" name="idInmueble" value="<%= i.getId() %>">
                            <button class="btn btn-peligro btn-peq" type="submit">Retirar</button>
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
