package com.rentainmuebles.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Cifrado de contraseñas. En la base se guarda "sha256_" + huella SHA-256,
 * nunca la contraseña original ("contraseña encriptada" del CU-01).
 */
public class Seguridad {

    /** Texto que se guarda en usuario.contrasena para una contraseña dada. */
    public static String cifrar(String contrasena) {
        return "sha256_" + sha256(contrasena);
    }

    /** Calcula la huella SHA-256 de un texto en hexadecimal (64 caracteres). */
    public static String sha256(String texto) {
        try {
            MessageDigest algoritmo = MessageDigest.getInstance("SHA-256");
            byte[] huella = algoritmo.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(huella);   // bytes -> "8ce8fb5d..."
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Java no tiene SHA-256", e);   // no ocurre en la práctica
        }
    }
}
