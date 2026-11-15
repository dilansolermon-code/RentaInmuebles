package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Inmueble;
import com.rentainmuebles.modelo.Propietario;
import com.rentainmuebles.util.Imagenes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;

/**
 * CU-05 Registrar o retirar inmueble. El campo oculto "accion" dice qué hacer:
 * "guardar" (registrar, o editar con «include» Actualizar datos) o "retirar".
 * @MultipartConfig permite recibir archivos: la foto del inmueble.
 */
@WebServlet("/propietario/inmueble")
@MultipartConfig
public class InmuebleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String inicio = request.getContextPath();

        Propietario propietario = (Propietario) Validar.usuarioConRol(request, "propietario");
        if (propietario == null) {
            response.sendRedirect(inicio + "/login.jsp?error=sesion");
            return;
        }

        String accion = request.getParameter("accion");
        int idInmueble = Validar.entero(request.getParameter("idInmueble"));   // 0 = inmueble nuevo
        String lista = inicio + "/propietario/inmuebles.jsp?";

        try {
            if ("retirar".equals(accion)) {
                // Verifica reservas activas futuras (diagrama de secuencia, pasos 10-12)
                Inmueble inmueble = propietario.buscarInmueblePropio(idInmueble);
                if (inmueble == null) {
                    response.sendRedirect(lista + "error=retirar");
                } else if (inmueble.tieneReservasActivas()) {
                    response.sendRedirect(lista + "error=vigentes");
                } else {
                    boolean ok = propietario.retirarInmueble(idInmueble);
                    response.sendRedirect(lista + (ok ? "retirado=1" : "error=retirar"));
                }
                return;
            }

            // ---- accion "guardar": leer y validar (campos y valores numéricos mayores que cero) ----
            String direccion = request.getParameter("direccion") == null ? "" : request.getParameter("direccion").trim();
            double area      = Validar.decimal(request.getParameter("area"));
            int habitaciones = Validar.entero(request.getParameter("habitaciones"));
            int capacidad    = Validar.entero(request.getParameter("capacidad"));
            int idCiudad     = Validar.entero(request.getParameter("idCiudad"));
            double precio    = Validar.decimal(request.getParameter("precio"));

            if (direccion.isEmpty() || area <= 0 || habitaciones < 1 || capacidad < 1 || idCiudad < 1 || precio <= 0) {
                response.sendRedirect(inicio + "/propietario/registrar.jsp?error=datos"
                                      + (idInmueble > 0 ? "&id=" + idInmueble : ""));
                return;
            }

            // ---- la foto (opcional): se revisa antes de guardar nada ----
            Part foto = leerFoto(request);
            String errorFoto = Imagenes.validar(foto);
            if (errorFoto != null) {
                response.sendRedirect(inicio + "/propietario/registrar.jsp?error=" + errorFoto
                                      + (idInmueble > 0 ? "&id=" + idInmueble : ""));
                return;
            }

            if (idInmueble == 0) {
                Inmueble nuevo = propietario.registrarInmueble(direccion, area, habitaciones, capacidad, precio, idCiudad);
                if (nuevo != null) {
                    Imagenes.guardar(nuevo.getId(), foto);                  // la foto se llama con el id nuevo
                }
                response.sendRedirect(lista + "registrado=1");
            } else {
                boolean ok = propietario.actualizarDatos(idInmueble, direccion, area, habitaciones, capacidad, precio, idCiudad);
                if (ok) {
                    Imagenes.guardar(idInmueble, foto);                     // reemplaza la foto si eligió una nueva
                }
                response.sendRedirect(lista + (ok ? "actualizado=1" : "error=editar"));
            }
        } catch (SQLException e) {
            getServletContext().log("Error en InmuebleServlet", e);
            response.sendRedirect(lista + "error=bd");
        }
    }

    /** Lee el archivo del campo "foto" del formulario; null si no viene. */
    private Part leerFoto(HttpServletRequest request) throws IOException {
        try {
            return request.getPart("foto");
        } catch (ServletException | IllegalStateException e) {
            return null;                                                  // el formulario no traía archivo
        }
    }
}
