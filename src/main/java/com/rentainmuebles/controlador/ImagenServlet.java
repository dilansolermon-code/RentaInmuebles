package com.rentainmuebles.controlador;

import com.rentainmuebles.util.Imagenes;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Envía al navegador la foto de un inmueble: /RentaInmuebles/imagen?id=15
 * Hace falta porque la carpeta de fotos está fuera del proyecto web.
 */
@WebServlet("/imagen")
public class ImagenServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Validar.entero(request.getParameter("id"));
        Path ruta = Imagenes.buscar(id);
        if (ruta == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);         // 404: no hay foto
            return;
        }
        String nombre = ruta.getFileName().toString();
        String tipo = nombre.endsWith(".png") ? "image/png"
                    : (nombre.endsWith(".webp") ? "image/webp" : "image/jpeg");
        response.setContentType(tipo);
        Files.copy(ruta, response.getOutputStream());                     // copia el archivo a la respuesta
    }
}
