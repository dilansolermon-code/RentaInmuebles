<%-- INICIAR SESIÓN (Usuario.iniciarSesion): el formulario lo procesa LoginServlet --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String titulo = "Iniciar sesión";
    String rol = "publico";
    String error = request.getParameter("error");   // "datos", "inactiva", "bd" o "sesion"
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<div class="angosto">
    <% if ("rol".equals(request.getParameter("registrado"))) { %>
        <div class="alerta alerta-ok">Rol agregado a tu cuenta. Al iniciar sesión podrás elegir con cuál ingresar.</div>
    <% } else if (request.getParameter("registrado") != null) { %>
        <div class="alerta alerta-ok">Registro exitoso. Ya puedes iniciar sesión.</div>
    <% } %>
    <% if ("datos".equals(error)) { %>
        <div class="alerta alerta-aviso">Correo o contraseña incorrectos.</div>
    <% } else if ("inactiva".equals(error)) { %>
        <div class="alerta alerta-aviso">Tu cuenta está suspendida. Comunícate con el administrador de la plataforma.</div>
    <% } else if ("bd".equals(error)) { %>
        <div class="alerta alerta-aviso">No hay conexión con la base de datos. Revisa que PostgreSQL esté encendido y la contraseña en Conexion.java.</div>
    <% } else if ("sesion".equals(error)) { %>
        <div class="alerta alerta-aviso">Inicia sesión con una cuenta que tenga permiso para esa página.</div>
    <% } %>

    <div class="tarjeta">
        <h1>Iniciar sesión</h1>
        <p class="suave">Ingresa con el correo y la contraseña de tu cuenta.</p>

        <%-- method="post": la contraseña no queda visible en la dirección del navegador --%>
        <form class="formulario" action="<%= ctx %>/login" method="post">
            <div class="campo">
                <label for="email">Correo electrónico</label>
                <input type="email" id="email" name="email" placeholder="nombre@correo.com" required>
            </div>
            <div class="campo">
                <label for="contrasena">Contraseña</label>
                <input type="password" id="contrasena" name="contrasena" required>
            </div>
            <button class="btn btn-bloque" type="submit">Ingresar</button>
        </form>

        <p class="suave" style="margin-top:1rem">¿No tienes cuenta? <a href="registro.jsp">Regístrate aquí</a></p>
        <p class="ayuda">Usuarios de demostración (contraseña 1234): cliente@rentadias.com,
           propietario@rentadias.com y admin@rentadias.com.</p>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
