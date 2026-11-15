<%-- CU-03 REALIZAR RESERVA: detalle del inmueble (datos desde PostgreSQL) --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.Locale, java.time.LocalDate,
                 com.rentainmuebles.datos.InmuebleDAO,
                 com.rentainmuebles.modelo.Inmueble, com.rentainmuebles.modelo.Usuario" %>
<%
    String titulo = "Detalle del inmueble";
    Usuario u = (Usuario) session.getAttribute("usuario");
    String rol = (u != null && u.getRol().equals("cliente")) ? "cliente" : "publico";

    // 1. Buscar el inmueble por el id que llega en la dirección: detalle.jsp?id=3
    Inmueble inm = null;
    String errorBD = null;
    try {
        inm = new InmuebleDAO().buscarPorId(Integer.parseInt(request.getParameter("id")));
    } catch (NumberFormatException e) {
        // id vacío o no numérico: inm queda en null
    } catch (Exception e) {
        errorBD = e.getMessage();
    }

    // 2. Fechas sugeridas: desde mañana, 3 noches
    LocalDate manana = LocalDate.now().plusDays(1);
    LocalDate salidaSugerida = manana.plusDays(3);
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<p><a href="buscar.jsp">&larr; Volver a los resultados</a></p>

<% if (inm == null) { %>
    <div class="alerta alerta-aviso">
        <%= errorBD != null ? "No se pudo consultar la base de datos: " + errorBD : "El inmueble no existe." %>
    </div>
<% } else {
       String precio = String.format(Locale.US, "%,.0f", inm.getPrecioNoche()).replace(',', '.');
%>
<div class="dos-col">
    <%-- Columna izquierda: información del inmueble --%>
    <section>
        <div class="foto foto-grande foto-<%= inm.getId() % 6 + 1 %> <%= com.rentainmuebles.util.Imagenes.existe(inm.getId()) ? "con-foto" : "" %>"<%= com.rentainmuebles.util.Imagenes.estilo(ctx, inm.getId()) %>><%= inm.getCiudad() %></div>
        <h1 style="margin-top:1rem"><%= com.rentainmuebles.util.Html.esc(inm.getDireccion()) %></h1>
        <p class="suave"><%= inm.getCiudad() %> · Publicado por <%= com.rentainmuebles.util.Html.esc(inm.getPropietario()) %></p>

        <div class="ficha">
            <div><strong><%= (int) inm.getAreaMts() %> m²</strong><span class="suave">Área</span></div>
            <div><strong><%= inm.getNumeroHabitaciones() %></strong><span class="suave">Habitaciones</span></div>
            <div><strong><%= inm.getCapacidad() %></strong><span class="suave">Personas</span></div>
            <div><strong>$<%= precio %></strong><span class="suave">Por noche</span></div>
        </div>
    </section>

    <%-- Columna derecha: elegir fechas y continuar al pago --%>
    <aside class="tarjeta">
        <p><span class="precio" style="font-size:1.4rem">$<%= precio %></span> <span class="suave">/ noche</span></p>

        <form class="formulario" action="pago.jsp" method="get">
            <input type="hidden" name="idInmueble" value="<%= inm.getId() %>">
            <div class="grid grid-2">
                <div class="campo">
                    <label for="entrada">Entrada</label>
                    <input type="date" id="entrada" name="entrada" min="<%= manana %>" value="<%= manana %>" onchange="calcularTotal()" required>
                </div>
                <div class="campo">
                    <label for="salida">Salida</label>
                    <input type="date" id="salida" name="salida" min="<%= manana %>" value="<%= salidaSugerida %>" onchange="calcularTotal()" required>
                </div>
            </div>
            <div class="campo">
                <label for="personas">Personas</label>
                <input type="number" id="personas" name="personas" min="1" max="<%= inm.getCapacidad() %>" value="1">
            </div>

            <div class="alerta alerta-ok" id="aviso">La disponibilidad se verifica al pagar</div>

            <div class="resumen">
                <div><span>$<%= precio %> x <span id="noches">3</span> noches</span><span id="subtotal"></span></div>
                <div class="total"><span>Total</span><span id="total"></span></div>
            </div>

            <% if (u == null) { %>
                <p class="ayuda">Para continuar debes <a href="<%= ctx %>/login.jsp">iniciar sesión</a> como cliente.</p>
            <% } %>
            <button class="btn btn-bloque" type="submit">Realizar reserva</button>
        </form>
    </aside>
</div>

<script>
    // Calcula noches y total en pantalla (el valor definitivo lo calcula el servidor al pagar)
    function calcularTotal() {
        var precioNoche = <%= (long) inm.getPrecioNoche() %>;                 // viene de la base de datos
        var entrada = new Date(document.getElementById('entrada').value);
        var salida  = new Date(document.getElementById('salida').value);
        var noches  = Math.round((salida - entrada) / 86400000);              // milisegundos de un día
        var aviso   = document.getElementById('aviso');
        if (isNaN(noches) || noches < 1) {
            aviso.className = 'alerta alerta-aviso';
            aviso.textContent = 'La fecha de salida debe ser posterior a la de entrada';
            noches = 0;
        } else {
            aviso.className = 'alerta alerta-ok';
            aviso.textContent = 'La disponibilidad se verifica al pagar';
        }
        var total = '$' + (precioNoche * noches).toLocaleString('es-CO');
        document.getElementById('noches').textContent = noches;
        document.getElementById('subtotal').textContent = total;
        document.getElementById('total').textContent = total;
    }
    calcularTotal();   // calcula al abrir la página
</script>
<% } %>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
