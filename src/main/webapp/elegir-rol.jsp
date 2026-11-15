<%-- ELEGIR ROL: aparece cuando una persona es Cliente y Propietario (o también Administrador) --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, com.rentainmuebles.modelo.Usuario, com.rentainmuebles.util.Html" %>
<%
    @SuppressWarnings("unchecked")
    List<Usuario> roles = (List<Usuario>) session.getAttribute("roles");
    if (roles == null || roles.isEmpty()) {                     // nadie ha iniciado sesión
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String titulo = "Elegir rol";
    String rol = "publico";
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<div class="angosto">
    <div class="tarjeta">
        <h1>¿Cómo quieres ingresar?</h1>
        <p class="suave">Hola, <%= Html.esc(roles.get(0).getNombre()) %>. Tu cuenta tiene varios roles; elige con cuál entrar.
           Después puedes cambiar de rol desde el menú.</p>

        <%-- Un botón por cada rol verificado con la contraseña --%>
        <form class="formulario" action="<%= ctx %>/rol" method="post">
        <% for (Usuario r : roles) {
               String texto = r.getRol().equals("admin") ? "Administrador"
                            : (r.getRol().equals("propietario") ? "Propietario: publicar y administrar mis inmuebles"
                                                                : "Cliente: buscar y reservar inmuebles");
        %>
            <button class="btn btn-bloque" type="submit" name="rol" value="<%= r.getRol() %>"
                    style="margin-bottom:.6rem"><%= texto %></button>
        <% } %>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
