package com.rentainmuebles.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Una reserva (tabla renta): qué cliente renta qué inmueble y en qué fechas.
 */
public class Renta {

    // Valores posibles de la columna estado
    public static final String ACTIVA = "activo";      // pagada
    public static final String CANCELADA = "inactivo";

    private int id;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private double valor;
    private String estado;        // ACTIVA o CANCELADA
    private int idInmueble;
    private String direccion;     // del inmueble rentado
    private String ciudad;
    private String banco;         // banco de la cuenta del cliente (puede ser null)

    public Renta(int id, LocalDate fechaEntrada, LocalDate fechaSalida, double valor, String estado,
                 int idInmueble, String direccion, String ciudad, String banco) {
        this.id = id;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.valor = valor;
        this.estado = estado;
        this.idInmueble = idInmueble;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.banco = banco;
    }

    /** Número de noches entre la entrada y la salida. */
    public long calcularNoches() {
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    /** true si la renta está activa (pagada). */
    public boolean estaActiva() {
        return ACTIVA.equals(estado);
    }

    /**
     * Texto que ve el usuario. La base solo guarda "activo" o "inactivo";
     * "Finalizada" se calcula: activa pero con la fecha de salida ya pasada.
     */
    public String getEstadoTexto() {
        if (!estaActiva()) {
            return "Cancelada";
        }
        if (!fechaSalida.isAfter(LocalDate.now())) {   // la salida es hoy o ya pasó
            return "Finalizada";
        }
        return "Pagada";
    }

    /** Solo se puede cancelar si está activa y todavía no ha comenzado. */
    public boolean sePuedeCancelar() {
        return estaActiva() && fechaEntrada.isAfter(LocalDate.now());
    }

    public int getId() { return id; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public double getValor() { return valor; }
    public String getEstado() { return estado; }
    public int getIdInmueble() { return idInmueble; }
    public String getDireccion() { return direccion; }
    public String getCiudad() { return ciudad; }
    public String getBanco() { return banco; }

    // Nombre del cliente: solo se llena en la vista del propietario
    private String cliente;
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
}
