<%-- MI PERFIL: datos del usuario con sesión (cliente o propietario). Lo guarda PerfilServlet --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList,
                 com.rentainmuebles.datos.UsuarioDAO, com.rentainmuebles.datos.CatalogoDAO,
                 com.rentainmuebles.modelo.Usuario, com.rentainmuebles.modelo.Perfil, com.rentainmuebles.modelo.Banco" %>
<%
    // 1. Solo clientes y propietarios con sesión
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || u.getRol().equals("admin")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    String titulo = "Mi perfil";
    String rol = u.getRol();                       // el menú cambia según el rol

    // 2. Datos actuales desde la base
    Perfil perfil = null;
    List<Banco> bancos = new ArrayList<>();
    String errorBD = null;
    try {
        perfil = new UsuarioDAO().obtenerPerfil(u.getId(), u.getRol());
        bancos = new CatalogoDAO().listarBancos();
    } catch (Exception e) {
        errorBD = e.getMessage();
    }

    String error = request.getParameter("error");
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<div class="medio">
    <% if (request.getParameter("guardado") != null) { %>
        <div class="alerta alerta-ok">Tus datos se actualizaron correctamente.</div>
    <% } %>
    <% if ("datos".equals(error)) { %>
        <div class="alerta alerta-aviso">Revisa los datos: nombre, correo y cuenta (6 a 16 dígitos).</div>
    <% } else if ("email".equals(error)) { %>
        <div class="alerta alerta-aviso">Ese correo ya lo usa otra cuenta.</div>
    <% } else if ("cuentaRepetida".equals(error)) { %>
        <div class="alerta alerta-aviso">Ese número de cuenta ya está registrado por otra persona. Escribe otro número de cuenta.</div>
    <% } else if ("bd".equals(error)) { %>
        <div class="alerta alerta-aviso">No se pudo guardar en la base de datos.</div>
    <% } %>
    <% if (errorBD != null) { %>
        <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= errorBD %></div>
    <% } %>

    <% if (perfil != null) { %>
    <div class="tarjeta">
        <h1>Mi perfil</h1>
        <p class="suave">Actualiza tus datos de contacto y tu cuenta bancaria.</p>

        <form class="formulario" action="<%= ctx %>/perfil" method="post">
            <fieldset>
                <legend>Datos personales</legend>
                <div class="grid grid-2">
                    <div class="campo">
                        <label for="nombre">Nombre completo</label>
                        <input id="nombre" name="nombre" value="<%= perfil.getNombre() %>" required>
                    </div>
                    <div class="campo">
                        <label for="telefono">Teléfono</label>
                        <input id="telefono" name="telefono" value="<%= perfil.getTelefono() != null ? perfil.getTelefono() : "" %>">
                    </div>
                    <div class="campo">
                        <label for="email">Correo electrónico</label>
                        <input id="email" name="email" type="email" value="<%= perfil.getEmail() %>" required>
                    </div>
                    <div class="campo">
                        <label>Tipo de cuenta en la plataforma</label>
                        <input value="<%= u.getRol().equals("cliente") ? "Cliente" : "Propietario" %>" disabled>
                    </div>
                </div>
            </fieldset>

            <fieldset>
                <legend>Datos bancarios</legend>
                <div class="grid grid-3">
                    <div class="campo">
                        <label for="idBanco">Banco</label>
                        <select id="idBanco" name="idBanco" required>
                            <option value="">-- Selecciona --</option>
                            <% for (Banco b : bancos) { %>
                                <option value="<%= b.getId() %>" <%= b.getId() == perfil.getIdBanco() ? "selected" : "" %>><%= b.getNombre() %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="campo">
                        <label for="numeroCta">Número de cuenta nuevo</label>
                        <input id="numeroCta" name="numeroCta" inputmode="numeric" pattern="[0-9]{6,16}"
                               placeholder="<%= perfil.getCuentaOculta().isEmpty() ? "Sin cuenta registrada" : "Actual: " + perfil.getCuentaOculta() %>">
                    </div>
                    <div class="campo">
                        <label for="tipoCta">Tipo de cuenta</label>
                        <select id="tipoCta" name="tipoCta">
                            <option <%= "Ahorros".equals(perfil.getTipoCta()) ? "selected" : "" %>>Ahorros</option>
                            <option <%= "Corriente".equals(perfil.getTipoCta()) ? "selected" : "" %>>Corriente</option>
                        </select>
                    </div>
                </div>
                <span class="ayuda">Por seguridad solo se muestran los últimos 4 dígitos. Deja el número vacío para conservar el actual.</span>
            </fieldset>

            <button class="btn" type="submit">Guardar cambios</button>
        </form>
    </div>
    <% } %>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
