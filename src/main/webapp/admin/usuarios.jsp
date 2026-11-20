<%-- CU-08 SUSPENDER CUENTA (con «extend» Restaurar cuenta). Datos desde PostgreSQL --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList,
                 com.rentainmuebles.modelo.Usuario, com.rentainmuebles.modelo.Administrador, com.rentainmuebles.util.Html" %>
<%
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("admin")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    Administrador admin = (Administrador) u;
    String titulo = "Gestionar cuentas";
    String rol = "admin";

    String texto = request.getParameter("buscar") == null ? "" : request.getParameter("buscar").trim();
    List<Usuario> usuarios = new ArrayList<>();
    String errorBD = null;
    try {
        usuarios = admin.buscarCuentas(texto);   // consulta cuentas coincidentes
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
    String error = request.getParameter("error");
    String cambiado = request.getParameter("cambiado");
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (Usuario.INACTIVO.equals(cambiado)) { %>
    <div class="alerta alerta-aviso">Cuenta suspendida correctamente.</div>
<% } else if (Usuario.ACTIVO.equals(cambiado)) { %>
    <div class="alerta alerta-ok">Cuenta restaurada. El usuario recuperó el acceso al sistema.</div>
<% } %>
<% if ("propia".equals(error)) { %>
    <div class="alerta alerta-aviso">No puedes suspender tu propia cuenta.</div>
<% } else if (error != null) { %>
    <div class="alerta alerta-aviso">No se pudo cambiar el estado de la cuenta.</div>
<% } %>
<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
<% } %>

<div class="titulo-seccion">
    <div>
        <h1>Gestionar cuentas</h1>
        <p class="suave">Busca por nombre o correo. Se muestran hasta 50 resultados.</p>
    </div>
    <form action="usuarios.jsp" method="get" style="display:flex;gap:.5rem">
        <input name="buscar" value="<%= Html.esc(texto) %>" placeholder="Nombre o correo" style="min-width:240px">
        <button class="btn" type="submit">Buscar</button>
    </form>
</div>

<div class="tarjeta tabla-envoltura" style="padding:0">
    <table>
        <thead>
            <tr><th>Id</th><th>Nombre</th><th>Correo</th><th>Tipo</th><th>Calificación</th><th>Estado</th><th>Acción</th></tr>
        </thead>
        <tbody>
        <% if (usuarios.isEmpty() && errorBD == null) { %>
            <tr><td colspan="7" class="suave">No se encontraron usuarios con esos datos.</td></tr>
        <% } %>
        <% for (Usuario x : usuarios) {
               boolean activa = x.estaActiva();
        %>
            <tr>
                <td><%= x.getId() %></td>
                <td><%= Html.esc(x.getNombre()) %></td>
                <td><%= Html.esc(x.getEmail()) %></td>
                <td><%= Html.esc(x.getDescripcionRoles()) %></td>
                <%-- Polimorfismo: cada tipo de usuario responde a su manera --%>
                <td><%= x.getCalificacionTexto() %></td>
                <td><span class="insignia <%= activa ? "ok" : "mal" %>"><%= activa ? "Activa" : "Suspendida" %></span></td>
                <td>
                    <% if (x.getId() != u.getId()) { %>
                    <form action="<%= ctx %>/admin/estado-cuenta" method="post" style="display:inline"
                          onsubmit="return confirm('<%= activa ? "¿Suspender esta cuenta? El usuario no podrá iniciar sesión." : "¿Restaurar esta cuenta?" %>')">
                        <input type="hidden" name="idUsuario" value="<%= x.getId() %>">
                        <input type="hidden" name="estado" value="<%= activa ? Usuario.INACTIVO : Usuario.ACTIVO %>">
                        <button class="btn <%= activa ? "btn-peligro" : "" %> btn-peq" type="submit"><%= activa ? "Suspender" : "Restaurar cuenta" %></button>
                    </form>
                    <% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
