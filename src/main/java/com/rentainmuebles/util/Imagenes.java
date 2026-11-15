package com.rentainmuebles.util;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Fotos de los inmuebles guardadas como archivos (no en la base de datos).
 * Cada foto se llama como el id del inmueble: 15.jpg, 16.png...
 * La carpeta está FUERA del proyecto para que "Clean and Build" no la borre:
 * en Windows queda en C:\Users\TU_USUARIO\RentaDias\imagenes
 */
public class Imagenes {

    public static final Path CARPETA = Paths.get(System.getProperty("user.home"), "RentaDias", "imagenes");
    private static final String[] EXTENSIONES = {"jpg", "png", "webp"};
    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024;          // 5 MB

    /** Devuelve la ruta de la foto del inmueble, o null si no tiene. */
    public static Path buscar(int idInmueble) {
        for (String ext : EXTENSIONES) {
            Path ruta = CARPETA.resolve(idInmueble + "." + ext);
            if (Files.exists(ruta)) {
                return ruta;
            }
        }
        return null;
    }

    /** true si el inmueble tiene foto. */
    public static boolean existe(int idInmueble) {
        return buscar(idInmueble) != null;
    }

    /**
     * Revisa la foto antes de guardar. Devuelve null si está bien (o si no se eligió foto)
     * y un texto con el problema si no sirve.
     */
    public static String validar(Part foto) {
        if (foto == null || foto.getSize() == 0) {
            return null;                                                  // la foto es opcional
        }
        if (extension(foto) == null) {
            return "formato";                                             // no es JPG, PNG ni WEBP
        }
        if (foto.getSize() > TAMANO_MAXIMO) {
            return "tamano";
        }
        return null;
    }

    /** Guarda (o reemplaza) la foto del inmueble. Si no se eligió foto, no hace nada. */
    public static void guardar(int idInmueble, Part foto) throws IOException {
        if (foto == null || foto.getSize() == 0) {
            return;
        }
        Files.createDirectories(CARPETA);                                 // crea la carpeta si no existe
        Path anterior = buscar(idInmueble);
        if (anterior != null) {
            Files.delete(anterior);                                       // borra la foto vieja (puede tener otra extensión)
        }
        Path destino = CARPETA.resolve(idInmueble + "." + extension(foto));
        try (InputStream entrada = foto.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Atributo style para mostrar la foto como fondo de un recuadro, o "" si no tiene.
     * El parámetro v (fecha del archivo) obliga al navegador a mostrar la foto nueva cuando se cambia.
     */
    public static String estilo(String ctx, int idInmueble) {
        Path ruta = buscar(idInmueble);
        if (ruta == null) {
            return "";
        }
        long version = ruta.toFile().lastModified();
        return " style=\"background-image:url('" + ctx + "/imagen?id=" + idInmueble + "&v=" + version + "')\"";
    }

    /** Extensión según el tipo de archivo que informa el navegador; null si no es una imagen permitida. */
    private static String extension(Part foto) {
        String tipo = foto.getContentType();
        if ("image/jpeg".equals(tipo)) return "jpg";
        if ("image/png".equals(tipo))  return "png";
        if ("image/webp".equals(tipo)) return "webp";
        return null;
    }
}
