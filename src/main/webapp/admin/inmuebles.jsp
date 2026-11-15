<%-- ADMINISTRADOR: inmuebles publicados, con opción de retirarlos. Datos desde PostgreSQL --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList, java.util.Locale,
                 com.rentainmuebles.datos.InmuebleDAO, com.rentainmuebles.modelo.Inmueble,
                 com.rentainmuebles.modelo.Usuario, com.rentainmuebles.util.Html" %>
<%
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("admin")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    String titulo = "Inmuebles publicados";
    String rol = "admin";

    String texto = request.getParameter("buscar") == null ? "" : request.getParameter("buscar").trim();
    List<Inmueble> inmuebles = new ArrayList<>();
    String errorBD = null;
    try {
        inmuebles = new InmuebleDAO().buscarComoAdmin(texto);
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
    String error = request.getParameter("error");
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (request.getParameter("retirado") != null) { %>
    <div class="alerta alerta-aviso">El inmueble fue retirado de la plataforma. Su historial de rentas se conserva.</div>
<% } %>
<% if ("vigentes".equals(error)) { %>
    <div class="alerta alerta-aviso">No se puede retirar: el inmueble tiene reservas activas que todavía no terminan.</div>
<% } else if (error != null) { %>
    <div class="alerta alerta-aviso">No se pudo retirar el inmueble.</div>
<% } %>
<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
<% } %>

<div class="titulo-seccion">
    <div>
        <h1>Inmuebles publicados</h1>
        <p class="suave">Revisa los inmuebles de la plataforma y retira los que no cumplan las condiciones. Se muestran hasta 100.</p>
    </div>
    <form action="inmuebles.jsp" method="get" style="display:flex;gap:.5rem">
        <input name="buscar" value="<%= Html.esc(texto) %>" placeholder="Dirección, ciudad o propietario" style="min-width:260px">
        <button class="btn" type="submit">Buscar</button>
    </form>
</div>

<div class="tarjeta tabla-envoltura" style="padding:0">
    <table>
        <thead>
            <tr><th>Id</th><th>Dirección</th><th>Ciudad</th><th>Propietario</th><th>Capacidad</th><th>Precio/noche</th><th>Acción</th></tr>
        </thead>
        <tbody>
        <% if (inmuebles.isEmpty() && errorBD == null) { %>
            <tr><td colspan="7" class="suave">No hay inmuebles que coincidan con la búsqueda.</td></tr>
        <% } %>
        <% for (Inmueble i : inmuebles) { %>
            <tr>
                <td><%= i.getId() %></td>
                <td><%= Html.esc(i.getDireccion()) %></td>
                <td><%= Html.esc(i.getCiudad()) %></td>
                <td><%= Html.esc(i.getPropietario()) %></td>
                <td><%= i.getCapacidad() %></td>
                <td>$<%= String.format(Locale.US, "%,.0f", i.getPrecioNoche()).replace(',', '.') %></td>
                <td>
                    <form action="<%= ctx %>/admin/retirar" method="post" style="display:inline"
                          onsubmit="return confirm('¿Seguro que deseas retirar este inmueble de la plataforma?')">
                        <input type="hidden" name="idInmueble" value="<%= i.getId() %>">
                        <button class="btn btn-peligro btn-peq" type="submit">Retirar</button>
                    </form>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
