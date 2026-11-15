<%-- PÁGINA DE INICIO (pública): presentación, buscador rápido y destinos (datos desde PostgreSQL) --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.Map, java.util.LinkedHashMap, java.util.TreeMap,
                 com.rentainmuebles.datos.CatalogoDAO, com.rentainmuebles.util.Html" %>
<%
    String titulo = "Inicio";     // texto de la pestaña
    String rol = "publico";       // menú para visitantes sin sesión

    // Ciudades con inmuebles publicados y cuántos tiene cada una (de mayor a menor)
    Map<String, Long> conteo = new LinkedHashMap<>();
    String errorBD = null;
    try {
        conteo = new CatalogoDAO().contarInmueblesPorCiudad();
    } catch (Exception e) {
        errorBD = e.getMessage();
    }
    // Para la lista desplegable: las mismas ciudades en orden alfabético (TreeMap ordena por nombre)
    Map<String, Long> alfabetico = new TreeMap<>(conteo);
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<section class="hero">
    <h1>Encuentra dónde quedarte, por los días que necesites</h1>
    <p>Apartamentos, casas y habitaciones en renta por noche, publicados directamente por sus propietarios.</p>

    <%-- CU-02 Buscar inmuebles: el formulario envía los filtros a la página de búsqueda --%>
    <form class="buscador" action="<%= ctx %>/cliente/buscar.jsp" method="get">
        <div class="campo">
            <label for="ciudad">Ciudad</label>
            <select id="ciudad" name="ciudad">
                <option value="">Todas las ciudades</option>
                <% for (Map.Entry<String, Long> c : alfabetico.entrySet()) { %>
                    <option value="<%= Html.esc(c.getKey()) %>"><%= Html.esc(c.getKey()) %> (<%= c.getValue() %>)</option>
                <% } %>
            </select>
        </div>
        <div class="campo">
            <label for="entrada">Entrada</label>
            <input type="date" id="entrada" name="entrada">
        </div>
        <div class="campo">
            <label for="salida">Salida</label>
            <input type="date" id="salida" name="salida">
        </div>
        <div class="campo">
            <label for="personas">Personas</label>
            <input type="number" id="personas" name="personas" min="1" value="2">
        </div>
        <button class="btn" type="submit">Buscar</button>
    </form>
</section>

<div class="titulo-seccion">
    <h2>Destinos destacados</h2>
    <a href="<%= ctx %>/cliente/buscar.jsp">Ver todos los inmuebles</a>
</div>
<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= Html.esc(errorBD) %></div>
<% } else if (conteo.isEmpty()) { %>
    <p class="suave">Aún no hay inmuebles publicados.</p>
<% } %>
<%-- Las 4 ciudades con más inmuebles publicados --%>
<div class="grid grid-4">
<% int n = 0;
   for (Map.Entry<String, Long> c : conteo.entrySet()) {
       if (n == 4) break;                                     // solo las 4 primeras
       n++;
       String enlace = ctx + "/cliente/buscar.jsp?ciudad=" + java.net.URLEncoder.encode(c.getKey(), "UTF-8");
%>
    <a class="tarjeta inmueble" href="<%= enlace %>" style="text-decoration:none;color:inherit">
        <div class="foto foto-<%= n %>"><%= Html.esc(c.getKey()) %></div>
        <div class="cuerpo"><span class="datos"><%= c.getValue() %> <%= c.getValue() == 1 ? "inmueble disponible" : "inmuebles disponibles" %></span></div>
    </a>
<% } %>
</div>

<h2 style="margin-top:2rem">¿Cómo funciona?</h2>
<div class="grid grid-3">
    <div class="tarjeta paso">
        <div class="numero">1</div>
        <h3>Busca</h3>
        <p class="suave">Filtra por ciudad y número de personas.</p>
    </div>
    <div class="tarjeta paso">
        <div class="numero">2</div>
        <h3>Reserva</h3>
        <p class="suave">El sistema verifica la disponibilidad y calcula el total.</p>
    </div>
    <div class="tarjeta paso">
        <div class="numero">3</div>
        <h3>Disfruta</h3>
        <p class="suave">Consulta o cancela tus reservas desde tu cuenta.</p>
    </div>
</div>

<div class="tarjeta llamado">
    <div>
        <h3>¿Tienes un inmueble para rentar?</h3>
        <p class="suave">Regístrate como propietario y publícalo: aparece de inmediato en las búsquedas.</p>
    </div>
    <a class="btn" href="<%= ctx %>/registro.jsp">Publicar mi inmueble</a>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
