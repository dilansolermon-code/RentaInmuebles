package com.rentainmuebles.util;

/**
 * Protege el texto antes de mostrarlo en una página.
 * Si alguien escribe por ejemplo <script> en su nombre o en una dirección,
 * se muestra como texto y no se ejecuta en el navegador de otros usuarios.
 */
public class Html {

    public static String esc(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")      // primero el &, para no dañar los demás
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
