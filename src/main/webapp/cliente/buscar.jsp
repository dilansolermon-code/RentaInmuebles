<%-- CU-02 BUSCAR INMUEBLES (con «extend» Filtrar por ciudad y capacidad). Datos desde PostgreSQL --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList, java.util.Locale,
                 com.rentainmuebles.datos.InmuebleDAO, com.rentainmuebles.datos.CatalogoDAO,
                 com.rentainmuebles.modelo.Inmueble, com.rentainmuebles.modelo.Usuario" %>
<%
    String titulo = "Buscar inmuebles";
    // Buscar es público: si hay un cliente con sesión se muestra su menú
    Usuario u = (Usuario) session.getAttribute("usuario");
    String rol = (u != null && u.getRol().equals("cliente")) ? "cliente" : "publico";

    // 1. Leer los filtros del formulario
    String ciudad = request.getParameter("ciudad");
    if (ciudad == null) {
        ciudad = "";                                   // "" = todas las ciudades
    }
    int personas = 1;
    try {
        personas = Integer.parseInt(request.getParameter("personas"));
    } catch (Exception e) {
        // si no llega o no es número, se deja en 1
    }

    // 2. Consultar la base de datos
    List<String> ciudades = new ArrayList<>();
    List<Inmueble> inmuebles = new ArrayList<>();
    String errorBD = null;
    try {
        ciudades = new CatalogoDAO().listarCiudades();
        inmuebles = new InmuebleDAO().buscar(ciudad, personas);
    } catch (Exception e) {
        errorBD = e.getMessage();                      // se muestra en pantalla para saber qué falló
    }
    String textoCiudad = ciudades.contains(ciudad) ? ciudad : "todas las ciudades";
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<% if (errorBD != null) { %>
    <div class="alerta alerta-aviso">No se pudo consultar la base de datos: <%= errorBD %></div>
<% } %>

<div class="col-filtros">
    <%-- Panel de filtros --%>
    <aside class="tarjeta">
        <h3>Filtrar</h3>
        <form class="formulario" action="buscar.jsp" method="get">
            <div class="campo">
                <label for="ciudad">Ciudad</label>
                <select id="ciudad" name="ciudad">
                    <option value="">Todas</option>
                    <% for (String c : ciudades) { %>
                        <option <%= c.equals(ciudad) ? "selected" : "" %>><%= c %></option>
                    <% } %>
                </select>
            </div>
            <div class="campo">
                <label for="personas">Capacidad mínima (personas)</label>
                <input type="number" id="personas" name="personas" min="1" value="<%= personas %>">
            </div>
            <button class="btn btn-bloque" type="submit">Aplicar filtros</button>
        </form>
    </aside>

    <%-- Resultados: una tarjeta por cada inmueble que devolvió la consulta --%>
    <section>
        <div class="titulo-seccion" style="margin-top:0">
            <div>
                <h1>Inmuebles disponibles</h1>
                <p class="suave">Resultados en <strong><%= textoCiudad %></strong> para <%= personas %> persona(s) · <%= inmuebles.size() %> encontrados</p>
            </div>
        </div>

        <% if (inmuebles.isEmpty() && errorBD == null) { %>
            <div class="tarjeta">No se encontraron inmuebles con esos criterios.</div>
        <% } %>

        <div class="grid grid-3">
        <% for (Inmueble i : inmuebles) {
               String precio = String.format(Locale.US, "%,.0f", i.getPrecioNoche()).replace(',', '.');
               int color = i.getId() % 6 + 1;              // elige uno de los 6 colores de "foto"
        %>
            <div class="tarjeta inmueble">
                <div class="foto foto-<%= color %> <%= com.rentainmuebles.util.Imagenes.existe(i.getId()) ? "con-foto" : "" %>"<%= com.rentainmuebles.util.Imagenes.estilo(ctx, i.getId()) %>><%= i.getCiudad() %></div>
                <div class="cuerpo">
                    <h3><%= com.rentainmuebles.util.Html.esc(i.getDireccion()) %></h3>
                    <span class="datos"><%= i.getCiudad() %> · <%= (int) i.getAreaMts() %> m² · <%= i.getNumeroHabitaciones() %> hab. · <%= i.getCapacidad() %> personas</span>
                    <div class="pie-tarjeta">
                        <span class="precio">$<%= precio %> <small class="suave">/noche</small></span>
                        <a class="btn btn-peq" href="detalle.jsp?id=<%= i.getId() %>">Ver y reservar</a>
                    </div>
                </div>
            </div>
        <% } %>
        </div>
    </section>
</div>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
