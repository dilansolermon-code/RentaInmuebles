<%-- CU-03: resumen de la reserva y pago. Verifica la disponibilidad ANTES de mostrar el resumen.
     El formulario lo procesa PagarServlet --%>
<%@ page contentType="text/html;charset=UTF-8"
         import="java.util.List, java.util.ArrayList, java.util.Locale,
                 java.time.LocalDate, java.time.temporal.ChronoUnit, java.time.format.DateTimeFormatter,
                 com.rentainmuebles.modelo.Inmueble, com.rentainmuebles.modelo.Banco, com.rentainmuebles.modelo.Usuario" %>
<%
    // 1. Solo un cliente con sesión puede pagar
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null || !u.getRol().equals("cliente")) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sesion");
        return;
    }
    String titulo = "Realizar pago";
    String rol = "cliente";

    // 2. Inmueble y fechas que llegan desde detalle.jsp
    int idInmueble = 0;
    try { idInmueble = Integer.parseInt(request.getParameter("idInmueble")); } catch (Exception e) { }

    Inmueble inm = null;
    LocalDate entrada = null;
    LocalDate salida = null;
    double total = 0;
    long noches = 0;
    List<Banco> bancos = new ArrayList<>();
    String problema = null;                         // mensaje si algo impide pagar

    try {
        bancos = Banco.listarTodos();
        inm = Inmueble.buscarPublicado(idInmueble);
        entrada = LocalDate.parse(request.getParameter("entrada"));   // texto "2026-11-20" -> fecha
        salida = LocalDate.parse(request.getParameter("salida"));
        noches = ChronoUnit.DAYS.between(entrada, salida);

        if (inm == null) {
            problema = "El inmueble no existe.";
        } else if (noches < 1 || entrada.isBefore(LocalDate.now())) {
            problema = "Las fechas no son válidas: la entrada debe ser desde hoy y la salida posterior.";
        } else if (!inm.estaDisponible(entrada, salida)) {
            // «include» Verificar disponibilidad (paso 4 del CU-03), antes de mostrar el resumen
            problema = "El inmueble no está disponible en las fechas seleccionadas. Por favor, elija otras fechas.";
        } else {
            total = inm.calcularPrecio(noches);     // paso 5: calcula el valor total
        }
    } catch (java.time.format.DateTimeParseException | NullPointerException e) {
        problema = "Las fechas no son válidas. Vuelve y elígelas de nuevo.";
    } catch (Exception e) {
        problema = "No se pudo consultar la base de datos: " + e.getMessage();
    }

    // 3. Mensajes de error que devuelve PagarServlet
    String error = request.getParameter("error");
    String mensajeError = null;
    if ("cuenta".equals(error))  mensajeError = "Revisa el banco, el tipo y el número de cuenta (6 a 16 dígitos).";
    if ("fechas".equals(error))  mensajeError = "Las fechas no son válidas: la entrada debe ser desde hoy y la salida posterior.";
    if ("ocupado".equals(error)) mensajeError = "El inmueble no está disponible en las fechas seleccionadas. Por favor, elija otras fechas.";
    if ("pago".equals(error))    mensajeError = "El banco no confirmó el pago. Revisa los datos de la cuenta.";
    if ("cuentaRepetida".equals(error)) mensajeError = "Ese número de cuenta ya está registrado por otra persona. Escribe otro número de cuenta.";
    if ("bd".equals(error))      mensajeError = "Ocurrió un error al guardar en la base de datos. Intenta de nuevo.";

    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String totalTexto = String.format(Locale.US, "%,.0f", total).replace(',', '.');
%>
<%@ include file="/WEB-INF/jspf/encabezado.jspf" %>

<p><a href="detalle.jsp?id=<%= idInmueble %>">&larr; Volver</a></p>

<% if (mensajeError != null) { %>
    <div class="alerta alerta-aviso"><%= mensajeError %></div>
<% } %>

<% if (problema != null) { %>
    <div class="alerta alerta-aviso"><%= com.rentainmuebles.util.Html.esc(problema) %></div>
<% } else { %>
<div class="dos-col">
    <%-- Columna izquierda: formulario de pago --%>
    <section class="tarjeta">
        <h1>Confirmar reserva y pago</h1>
        <p class="suave">Las fechas están disponibles. Elige el banco desde el que vas a pagar; el valor se abona en la cuenta del propietario.</p>

        <form class="formulario" action="<%= ctx %>/cliente/pagar" method="post">
            <%-- Datos ocultos para PagarServlet --%>
            <input type="hidden" name="idInmueble" value="<%= idInmueble %>">
            <input type="hidden" name="entrada" value="<%= entrada %>">
            <input type="hidden" name="salida" value="<%= salida %>">
            <input type="hidden" name="nombreBanco" id="nombreBanco" value="">

            <div class="campo">
                <label for="idBanco">Banco</label>
                <%-- Al elegir el banco, su nombre se copia en el campo oculto nombreBanco --%>
                <select id="idBanco" name="idBanco" required
                        onchange="document.getElementById('nombreBanco').value = this.options[this.selectedIndex].text">
                    <option value="">-- Selecciona tu banco --</option>
                    <% for (Banco b : bancos) { %>
                        <option value="<%= b.getId() %>"><%= b.getNombre() %></option>
                    <% } %>
                </select>
            </div>

            <div class="grid grid-2">
                <div class="campo">
                    <label for="tipoCta">Tipo de cuenta</label>
                    <select id="tipoCta" name="tipoCta">
                        <option>Ahorros</option>
                        <option>Corriente</option>
                    </select>
                </div>
                <div class="campo">
                    <label for="numeroCta">Número de cuenta</label>
                    <input id="numeroCta" name="numeroCta" inputmode="numeric" pattern="[0-9]{6,16}"
                           title="Solo números, entre 6 y 16 dígitos" placeholder="Ej: 12345678901" required>
                </div>
            </div>

            <div class="opciones">
                <label><input type="checkbox" required> Acepto las condiciones de la reserva y la política de cancelación</label>
            </div>

            <button class="btn btn-bloque" type="submit">Confirmar reserva y pagar $<%= totalTexto %></button>
            <span class="ayuda">Simulación para la demostración: no se mueve dinero real.</span>
        </form>

        <%-- «extend» Cancelar reserva (CU-03, curso alternativo 2): anula la operación sin registrar nada --%>
        <p style="margin-top:1rem">
            <a class="btn btn-sec btn-bloque" href="detalle.jsp?id=<%= idInmueble %>&cancelada=1">Cancelar reserva</a>
        </p>
    </section>

    <%-- Columna derecha: resumen --%>
    <aside class="tarjeta">
        <h3>Resumen de la reserva</h3>
        <p><strong><%= com.rentainmuebles.util.Html.esc(inm.getDireccion()) %></strong><br><span class="suave"><%= inm.getCiudad() %></span></p>
        <div class="resumen">
            <div><span>Entrada</span><span><%= entrada.format(formato) %></span></div>
            <div><span>Salida</span><span><%= salida.format(formato) %></span></div>
            <div><span>Noches</span><span><%= noches %></span></div>
            <div class="total"><span>Total a pagar</span><span>$<%= totalTexto %></span></div>
        </div>
    </aside>
</div>
<% } %>

<%@ include file="/WEB-INF/jspf/pie.jspf" %>
