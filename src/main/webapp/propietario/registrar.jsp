<%-- REGISTRAR O EDITAR INMUEBLE (propietario). Lo procesa InmuebleServlet --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList,
                 com.rentainmuebles.datos.InmuebleDAO, com.rentainmuebles.datos.CatalogoDAO,
                 com.rentainmuebles.modelo.Inmueble, com.rentainmuebles.modelo.Ciudad, com.rentainmuebles.modelo.Usuario,
                 com.rentainmuebles.util.Html, com.rentainmuebles.util.Imagenes" %>
<%
    // 1. Solo propietarios con sesión
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("propietario")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    String rol = "propietario";

    // 2. ¿Nuevo o edición? registrar.jsp?id=5 edita el inmueble 5 (solo si es del propietario)
    int idInmueble = 0;
    try { idInmueble = Integer.parseInt(request.getParameter("id")); } catch (Exception e) { }

    Inmueble inm = null;
    List<Ciudad> ciudades = new ArrayList<>();
    String errorBD = null;
    try {
        ciudades = new CatalogoDAO().listarCiudadesConId();
        if (idInmueble > 0) {
            inm = new InmuebleDAO().buscarDelPropietario(u.getId(), idInmueble);
        }
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
    boolean editar = (inm != null);
    String titulo = editar ? "Editar inmueble" : "Registrar inmueble";
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<div class="medio">
    <p><a href="inmuebles.jsp">&larr; Volver a mis inmuebles</a></p>

    <% if ("datos".equals(request.getParameter("error"))) { %>
        <div class="alerta alerta-aviso">Complete todos los campos correctamente: los valores numéricos deben ser mayores que cero.</div>
    <% } %>
    <% if ("formato".equals(request.getParameter("error"))) { %>
        <div class="alerta alerta-aviso">La foto debe ser una imagen JPG, PNG o WEBP.</div>
    <% } else if ("tamano".equals(request.getParameter("error"))) { %>
        <div class="alerta alerta-aviso">La foto pesa más de 5 MB. Elige una imagen más liviana.</div>
    <% } %>
    <% if (errorBD != null) { %>
        <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
    <% } %>
    <% if (idInmueble > 0 && !editar && errorBD == null) { %>
        <div class="alerta alerta-aviso">Ese inmueble no existe o no es tuyo.</div>
    <% } %>

    <div class="tarjeta">
        <h1><%= titulo %></h1>
        <p class="suave">Los datos se guardan en las tablas inmueble y propietario_inmueble.</p>

        <%-- enctype="multipart/form-data": necesario para enviar archivos (la foto) --%>
        <form class="formulario" action="<%= ctx %>/propietario/inmueble" method="post" enctype="multipart/form-data">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="idInmueble" value="<%= editar ? inm.getId() : 0 %>">

            <div class="campo">
                <label for="direccion">Dirección</label>
                <input id="direccion" name="direccion" required maxlength="150"
                       value="<%= editar ? Html.esc(inm.getDireccion()) : "" %>" placeholder="Calle 00 # 00-00, barrio">
            </div>

            <div class="grid grid-2">
                <div class="campo">
                    <label for="idCiudad">Ciudad (relación Ubica)</label>
                    <select id="idCiudad" name="idCiudad" required>
                        <% for (Ciudad c : ciudades) { %>
                            <option value="<%= c.getId() %>" <%= editar && c.getNombre().equals(inm.getCiudad()) ? "selected" : "" %>><%= Html.esc(c.getNombre()) %></option>
                        <% } %>
                    </select>
                </div>
                <div class="campo">
                    <label for="area">Área (m²)</label>
                    <input id="area" name="area" type="number" min="1" step="0.1" required value="<%= editar ? inm.getAreaMts() : "" %>">
                </div>
                <div class="campo">
                    <label for="habitaciones">Número de habitaciones</label>
                    <input id="habitaciones" name="habitaciones" type="number" min="1" required value="<%= editar ? inm.getNumeroHabitaciones() : "" %>">
                </div>
                <div class="campo">
                    <label for="capacidad">Capacidad (personas)</label>
                    <input id="capacidad" name="capacidad" type="number" min="1" required value="<%= editar ? inm.getCapacidad() : "" %>">
                </div>
                <div class="campo">
                    <label for="precio">Precio por noche (COP)</label>
                    <input id="precio" name="precio" type="number" min="1000" step="1000" required value="<%= editar ? (long) inm.getPrecioNoche() : "" %>">
                </div>
            </div>

            <%-- Foto del inmueble (opcional). Vista previa: la actual o la que se elija --%>
            <div class="campo">
                <label for="foto">Foto del inmueble (JPG, PNG o WEBP, máximo 5 MB)</label>
                <div id="vistaPrevia" class="foto foto-grande foto-<%= editar ? inm.getId() % 6 + 1 : 1 %> <%= editar && Imagenes.existe(inm.getId()) ? "con-foto" : "" %>"
                     <%= editar ? Imagenes.estilo(ctx, inm.getId()) : "" %>><%= editar && Imagenes.existe(inm.getId()) ? "Foto actual" : "Sin foto" %></div>
                <input id="foto" name="foto" type="file" accept="image/jpeg,image/png,image/webp" onchange="mostrarFoto(this)">
                <% if (editar) { %><p class="ayuda">Si no eliges una foto nueva, se conserva la actual.</p><% } %>
            </div>

            <button class="btn" type="submit"><%= editar ? "Guardar cambios" : "Registrar y publicar" %></button>
        </form>
    </div>
</div>

<script>
    // Muestra la foto elegida antes de enviarla (no la sube todavía)
    function mostrarFoto(campo) {
        if (campo.files.length === 0) return;                          // no eligió archivo
        var caja = document.getElementById('vistaPrevia');
        caja.style.backgroundImage = 'url(' + URL.createObjectURL(campo.files[0]) + ')';
        caja.className = caja.className + ' con-foto';
        caja.textContent = 'Foto nueva';
    }
</script>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
