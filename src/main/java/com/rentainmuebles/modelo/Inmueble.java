package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.InmuebleDAO;
import com.rentainmuebles.datos.RentaDAO;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Un inmueble disponible para renta.
 * Junta datos de las tablas inmueble, ciudad y propietario_inmueble (precio_noche).
 */
public class Inmueble {

    private int id;
    private String direccion;
    private double areaMts;
    private int numeroHabitaciones;
    private int capacidad;
    private String ciudad;        // nombre de la ciudad (relación Ubica)
    private double precioNoche;   // atributo de la relación Asocia
    private String propietario;   // nombre del propietario

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
}
