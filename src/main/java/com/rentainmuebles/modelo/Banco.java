package com.rentainmuebles.modelo;

/**
 * Banco (diagrama de clases). Sirve de dos formas:
 *  - como opción del catálogo (solo id y nombre), y
 *  - como la cuenta con la que el cliente paga (con número y tipo de cuenta:
 *    los atributos numero_cta y tipo_cta de la relación Vincula).
 */
public class Banco {

    private int id;
    private String nombre;
    private String numeroCta;
    private String tipoCta;

    /** Banco del catálogo. */
    public Banco(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /** Banco con la cuenta del usuario. */
    public Banco(int id, String nombre, String numeroCta, String tipoCta) {
        this(id, nombre);                 // reutiliza el constructor de arriba
        this.numeroCta = numeroCta;
        this.tipoCta = tipoCta;
    }

    /**
     * Debitar (diagrama de clases): SIMULA el cobro al cliente.
     * La aplicación no mueve dinero real: aprueba el pago si el valor es positivo
     * y la cuenta tiene un número válido.
     */
    public boolean debitar(double valor) {
        return valor > 0 && numeroCta != null && numeroCta.matches("[0-9]{6,16}");
    }

    /** Consignar (diagrama de clases): SIMULA el abono al propietario. */
    public boolean consignar(double valor) {
        return valor > 0 && numeroCta != null;
    }

    /** mostrarDatos (diagrama de clases): banco y cuenta con solo los últimos 4 dígitos visibles. */
    public String mostrarDatos() {
        if (numeroCta == null || numeroCta.length() < 4) {
            return nombre;
        }
        return nombre + " – " + tipoCta + " – ****" + numeroCta.substring(numeroCta.length() - 4);
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNumeroCta() { return numeroCta; }
    public String getTipoCta() { return tipoCta; }

    @Override
    public String toString() { return nombre; }   // lo que se ve en una lista desplegable
}
