package com.rentainmuebles.controlador;

import com.rentainmuebles.modelo.Usuario;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Validaciones que usan varios servlets. Los métodos son "static":
 * se llaman con el nombre de la clase, sin crear un objeto. Ej: Validar.entero("5")
 */
public class Validar {

    /** Convierte un texto en entero; si no es un número devuelve 0. */
    public static int entero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /** Convierte un texto en decimal; si no es un número devuelve 0. */
    public static double decimal(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (Exception e) {
            return 0;
        }
    }

    /** Cuenta válida: banco elegido, número de 6 a 16 dígitos y tipo Ahorros o Corriente. */
    public static boolean cuenta(int idBanco, String numeroCta, String tipoCta) {
        return idBanco > 0
                && numeroCta != null && numeroCta.matches("[0-9]{6,16}")
                && ("Ahorros".equals(tipoCta) || "Corriente".equals(tipoCta));
    }

    /** Devuelve el usuario de la sesión si tiene ese rol; si no, null. */
    public static Usuario usuarioConRol(HttpServletRequest request, String rol) {
        Usuario u = (Usuario) request.getSession().getAttribute("usuario");
        return (u != null && u.getRol().equals(rol)) ? u : null;
    }
}
