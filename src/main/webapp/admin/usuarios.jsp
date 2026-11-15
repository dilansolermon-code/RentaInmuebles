<%-- ADMINISTRADOR: consultar usuarios y sus roles. Datos desde PostgreSQL --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList,
                 com.rentainmuebles.modelo.Usuario, com.rentainmuebles.modelo.Administrador, com.rentainmuebles.util.Html" %>
<%
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("admin")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    String titulo = "Usuarios";
    String rol = "admin";

    String texto = request.getParameter("buscar") == null ? "" : request.getParameter("buscar").trim();
    List<Usuario> usuarios = new ArrayList<>();
    String errorBD = null;
    try {
        usuarios = ((Administrador) u).buscarCuentas(texto);   // CU-08: consulta cuentas coincidentes
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
<% } %>

<div class="titulo-seccion">
    <div>
        <h1>Usuarios registrados</h1>
        <p class="suave">Busca por nombre o correo. Se muestran hasta 50 resultados.</p>
        <p class="ayuda">Suspender y restaurar cuentas (CU-08) requiere una columna de estado en la tabla usuario.</p>
    </div>
    <form action="usuarios.jsp" method="get" style="display:flex;gap:.5rem">
        <input name="buscar" value="<%= Html.esc(texto) %>" placeholder="Nombre o correo" style="min-width:240px">
        <button class="btn" type="submit">Buscar</button>
    </form>
</div>

<div class="tarjeta tabla-envoltura" style="padding:0">
    <table>
        <thead>
            <tr><th>Id</th><th>Nombre</th><th>Correo</th><th>Roles</th></tr>
        </thead>
        <tbody>
        <% if (usuarios.isEmpty() && errorBD == null) { %>
            <tr><td colspan="4" class="suave">No se encontraron usuarios con esos datos.</td></tr>
        <% } %>
        <% for (Usuario x : usuarios) { %>
            <tr>
                <td><%= x.getId() %></td>
                <td><%= Html.esc(x.getNombre()) %></td>
                <td><%= Html.esc(x.getEmail()) %></td>
                <td><%= Html.esc(x.getDescripcionRoles()) %></td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
