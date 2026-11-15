package com.rentainmuebles.modelo;

/**
 * Datos que el usuario ve y edita en "Mi perfil":
 * los de la tabla usuario más su cuenta bancaria
 * (cuenta_cliente o cuenta_propietario, según el rol).
 */
public class Perfil {

    private String nombre;
    private String telefono;
    private String email;
    private int idBanco;        // 0 si todavía no tiene cuenta
    private String numeroCta;
    private String tipoCta;

    public Perfil(String nombre, String telefono, String email,
                  int idBanco, String numeroCta, String tipoCta) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.idBanco = idBanco;
        this.numeroCta = numeroCta;
        this.tipoCta = tipoCta;
    }

    /** Número de cuenta con solo los últimos 4 dígitos visibles (protección de datos). */
    public String getCuentaOculta() {
        if (numeroCta == null || numeroCta.length() < 4) {
            return "";
        }
        return "****" + numeroCta.substring(numeroCta.length() - 4);
    }

    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public int getIdBanco() { return idBanco; }
    public String getNumeroCta() { return numeroCta; }
    public String getTipoCta() { return tipoCta; }
}
