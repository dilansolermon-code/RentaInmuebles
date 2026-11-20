package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.InmuebleDAO;
import com.rentainmuebles.datos.RentaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Un inmueble para renta.
 * Junta datos de las tablas inmueble, ciudad y propietario_inmueble (precio_noche).
 */
public class Inmueble {

    // Valores posibles de estado_aprobacion (constantes: se escriben una sola vez)
    public static final String PENDIENTE = "pendiente";
    public static final String APROBADA = "aprobada";
    public static final String RECHAZADO = "rechazado";
    public static final String RETIRADO = "retirado";

    private int id;
    private String direccion;
    private double areaMts;
    private int numeroHabitaciones;
    private int capacidad;
    private String ciudad;             // nombre de la ciudad (relación Ubica)
    private double precioNoche;        // atributo de la relación Asocia
    private String propietario;        // nombre del propietario
    private int idPropietario;         // para abonarle el pago (relación Asocia)
    private String emailPropietario;   // lo ve el administrador al revisar (CU-07)
    private String estadoAprobacion = PENDIENTE;

    public Inmueble(int id, String direccion, double areaMts, int numeroHabitaciones,
                    int capacidad, String ciudad, double precioNoche, String propietario) {
        this.id = id;
        this.direccion = direccion;
        this.areaMts = areaMts;
        this.numeroHabitaciones = numeroHabitaciones;
        this.capacidad = capacidad;
        this.ciudad = ciudad;
        this.precioNoche = precioNoche;
        this.propietario = propietario;
    }

    // ======================= MÉTODOS DE CLASE (static) =======================

    /** CU-02: inmuebles APROBADOS de una ciudad ("" = todas) con capacidad para esas personas. */
    public static List<Inmueble> buscarDisponibles(String ciudad, int personas) throws SQLException {
        return new InmuebleDAO().buscar(ciudad, personas);
    }

    /** Un inmueble APROBADO por su id (detalle y pago). null si no existe o no está aprobado. */
    public static Inmueble buscarPublicado(int id) throws SQLException {
        return new InmuebleDAO().buscarPorId(id);
    }

    // ======================= MÉTODOS DEL OBJETO =======================

    /** Precio total por un número de noches. */
    public double calcularPrecio(long noches) {
        return precioNoche * noches;
    }

    /** estaDisponible (diagrama de clases): true si no hay otra renta activa que se cruce con esas fechas. */
    public boolean estaDisponible(LocalDate entrada, LocalDate salida) throws SQLException {
        return new RentaDAO().estaDisponible(id, entrada, salida);
    }

    /** true si tiene rentas activas que todavía no terminan (regla para poder retirarlo). */
    public boolean tieneReservasActivas() throws SQLException {
        return new InmuebleDAO().tieneRentasVigentes(id);
    }

    /** true si el administrador ya lo aprobó (solo así lo ven los clientes). */
    public boolean estaAprobado() {
        return APROBADA.equals(estadoAprobacion);
    }

    /** true si el propietario o el administrador lo retiró. */
    public boolean estaRetirado() {
        return RETIRADO.equals(estadoAprobacion);
    }

    /** Estado en palabras para las pantallas: Pendiente, Aprobado, Rechazado o Retirado. */
    public String getEstadoTexto() {
        if (APROBADA.equals(estadoAprobacion)) return "Aprobado";
        if (RECHAZADO.equals(estadoAprobacion)) return "Rechazado";
        if (RETIRADO.equals(estadoAprobacion)) return "Retirado";
        return "Pendiente";
    }

    /** consultarDetalles (diagrama de clases): resumen en una línea. */
    public String consultarDetalles() {
        return direccion + ", " + ciudad + " · " + (int) areaMts + " m² · "
             + numeroHabitaciones + " hab. · " + capacidad + " personas";
    }

    public int getId() { return id; }
    public String getDireccion() { return direccion; }
    public double getAreaMts() { return areaMts; }
    public int getNumeroHabitaciones() { return numeroHabitaciones; }
    public int getCapacidad() { return capacidad; }
    public String getCiudad() { return ciudad; }
    public double getPrecioNoche() { return precioNoche; }
    public String getPropietario() { return propietario; }

    public int getIdPropietario() { return idPropietario; }
    public void setIdPropietario(int idPropietario) { this.idPropietario = idPropietario; }
    public String getEmailPropietario() { return emailPropietario; }
    public void setEmailPropietario(String emailPropietario) { this.emailPropietario = emailPropietario; }
    public String getEstadoAprobacion() { return estadoAprobacion; }
    public void setEstadoAprobacion(String estadoAprobacion) { this.estadoAprobacion = estadoAprobacion; }
}
