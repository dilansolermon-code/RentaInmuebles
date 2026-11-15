<%-- CU-01 REGISTRARSE: crea una cuenta de Cliente o de Propietario. Lo procesa RegistroServlet --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList,
                 com.rentainmuebles.datos.CatalogoDAO, com.rentainmuebles.modelo.Ciudad, com.rentainmuebles.modelo.Banco" %>
<%
    String titulo = "Registrarse";
    String rol = "publico";

    // Ciudades y bancos desde la base de datos
    List<Ciudad> ciudades = new ArrayList<>();
    List<Banco> bancos = new ArrayList<>();
    String errorBD = null;
    try {
        CatalogoDAO catalogo = new CatalogoDAO();
        ciudades = catalogo.listarCiudadesConId();
        bancos = catalogo.listarBancos();
    } catch (Exception e) {
        errorBD = e.getMessage();
    }

    // Mensajes de error que devuelve RegistroServlet
    String error = request.getParameter("error");
    String mensaje = null;
    if ("datos".equals(error))  mensaje = "Complete todos los campos correctamente (contraseña de mínimo 4 caracteres).";
    if ("cuenta".equals(error)) mensaje = "Revisa la cuenta bancaria: elige el banco y escribe de 6 a 16 dígitos.";
    if ("email".equals(error))  mensaje = "El correo electrónico ya está en uso.";
    if ("cuentaRepetida".equals(error)) mensaje = "Ese número de cuenta ya está registrado por otra persona. Escribe otro número de cuenta.";
    if ("clave".equals(error))  mensaje = "Ese correo ya está registrado con otro rol. Para agregar este rol usa la misma contraseña de tu cuenta.";
    if ("bd".equals(error))     mensaje = "No se pudo guardar en la base de datos. Intenta de nuevo.";
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<div class="medio">
    <% if (mensaje != null) { %>
        <div class="alerta alerta-aviso"><%= mensaje %>
            <% if (request.getParameter("detalle") != null) { %>
                <br><small>Detalle: <%= com.rentainmuebles.util.Html.esc(request.getParameter("detalle")) %></small>
            <% } %>
        </div>
    <% } %>
    <% if (errorBD != null) { %>
        <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= errorBD %></div>
    <% } %>

    <div class="tarjeta">
        <h1>Crear cuenta</h1>
        <p class="suave">Elige si quieres rentar inmuebles o publicar los tuyos. Si ya tienes cuenta con el otro rol, usa el mismo correo y la misma contraseña.</p>

        <form class="formulario" action="<%= ctx %>/registro" method="post">
            <div class="opciones">
                <label><input type="radio" name="tipo" value="cliente" checked onchange="mostrarCampos()"> Quiero rentar (Cliente)</label>
                <label><input type="radio" name="tipo" value="propietario" onchange="mostrarCampos()"> Quiero publicar (Propietario)</label>
            </div>

            <%-- Tabla usuario --%>
            <fieldset>
                <legend>Datos personales</legend>
                <div class="grid grid-2">
                    <div class="campo">
                        <label for="nombre">Nombre completo</label>
                        <input id="nombre" name="nombre" required>
                    </div>
                    <div class="campo">
                        <label for="telefono">Teléfono</label>
                        <input id="telefono" name="telefono" type="tel" placeholder="3001234567">
                    </div>
                    <div class="campo">
                        <label for="email">Correo electrónico</label>
                        <input id="email" name="email" type="email" required>
                    </div>
                    <div class="campo">
                        <label for="contrasena">Contraseña (mínimo 4 caracteres)</label>
                        <input id="contrasena" name="contrasena" type="password" minlength="4" required>
                    </div>
                </div>
            </fieldset>

            <%-- Tabla cliente: id_ciudad_residencia (relación Reside) --%>
            <fieldset id="camposCliente">
                <legend>Datos del cliente</legend>
                <div class="campo">
                    <label for="idCiudad">Ciudad de residencia</label>
                    <select id="idCiudad" name="idCiudad">
                        <% for (Ciudad c : ciudades) { %>
                            <option value="<%= c.getId() %>"><%= c.getNombre() %></option>
                        <% } %>
                    </select>
                </div>
            </fieldset>

            <%-- Tabla propietario: tipo_persona (regla CHECK: Natural o Juridica) --%>
            <fieldset id="camposPropietario" style="display:none">
                <legend>Datos del propietario</legend>
                <div class="campo">
                    <label for="tipoPersona">Tipo de persona</label>
                    <select id="tipoPersona" name="tipoPersona">
                        <option value="Natural">Natural</option>
                        <option value="Juridica">Jurídica</option>
                    </select>
                </div>
            </fieldset>

            <%-- Tabla cuenta_cliente o cuenta_propietario --%>
            <fieldset>
                <legend>Datos bancarios</legend>
                <div class="grid grid-3">
                    <div class="campo">
                        <label for="idBanco">Banco</label>
                        <select id="idBanco" name="idBanco" required>
                            <option value="">-- Selecciona --</option>
                            <% for (Banco b : bancos) { %>
                                <option value="<%= b.getId() %>"><%= b.getNombre() %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="campo">
                        <label for="numeroCta">Número de cuenta</label>
                        <input id="numeroCta" name="numeroCta" inputmode="numeric" pattern="[0-9]{6,16}"
                               title="Solo números, entre 6 y 16 dígitos" required>
                    </div>
                    <div class="campo">
                        <label for="tipoCta">Tipo de cuenta</label>
                        <select id="tipoCta" name="tipoCta">
                            <option>Ahorros</option>
                            <option>Corriente</option>
                        </select>
                    </div>
                </div>
            </fieldset>

            <button class="btn btn-bloque" type="submit">Crear cuenta</button>
        </form>
    </div>
</div>

<script>
    // Muestra los campos del tipo de cuenta elegido y oculta los del otro
    function mostrarCampos() {
        var tipo = document.querySelector('input[name="tipo"]:checked').value;   // "cliente" o "propietario"
        document.getElementById('camposCliente').style.display = (tipo === 'cliente') ? '' : 'none';
        document.getElementById('camposPropietario').style.display = (tipo === 'propietario') ? '' : 'none';
    }
</script>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
