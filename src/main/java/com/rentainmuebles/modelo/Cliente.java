package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.InmuebleDAO;
import com.rentainmuebles.datos.RentaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Cliente (hereda de Usuario): busca, reserva y consulta sus reservas. */
public class Cliente extends Usuario {

    private String ciudadResidencia;   // nombre de la ciudad (relación Reside)
    private double calificacion;

    public Cliente(int id, String nombre, String telefono, String email, String contrasena,
                   String ciudadResidencia, double calificacion) {
        super(id, nombre, telefono, email, contrasena);   // llama al constructor de Usuario
        this.ciudadResidencia = ciudadResidencia;
        this.calificacion = calificacion;
    }

    /** CU-02: inmuebles de una ciudad ("" = todas) con capacidad para esas personas. */
    public List<Inmueble> buscarInmuebles(String ciudad, int personas) throws SQLException {
        return new InmuebleDAO().buscar(ciudad, personas);
    }

    /**
     * CU-03 Realizar reserva, en el orden del diagrama de secuencia:
     * verifica disponibilidad, calcula el valor, vincula el pago con el banco y registra la renta.
     * Devuelve la Renta creada, o null si el inmueble ya no está disponible o el banco rechaza el pago.
     */
    public Renta reservar(Inmueble inmueble, LocalDate entrada, LocalDate salida, Banco banco) throws SQLException {
        if (!inmueble.estaDisponible(entrada, salida)) {      // «include» Verificar disponibilidad
            return null;
        }
        long noches = java.time.temporal.ChronoUnit.DAYS.between(entrada, salida);
        double valor = inmueble.calcularPrecio(noches);       // calcula valor total
        if (!banco.debitar(valor)) {                          // vincula pago con el Banco
            return null;
        }
        int idRenta = new RentaDAO().pagar(getId(), inmueble.getId(), entrada, salida,
                                           valor, banco.getId(), banco.getNumeroCta(), banco.getTipoCta());
        return new Renta(idRenta, entrada, salida, valor, "activo", inmueble.getId(),
                         inmueble.getDireccion(), inmueble.getCiudad(), banco.getNombre());
    }

    /** CU-04: reservas del cliente, de la más reciente a la más antigua. */
    public List<Renta> consultarReservas() throws SQLException {
        return new RentaDAO().listarPorCliente(getId());
    }

    /** «extend» Cancelar reserva: deja la renta en estado inactivo. */
    public boolean cancelarReserva(int idRenta) throws SQLException {
        return new RentaDAO().cancelar(idRenta, getId());
    }

    @Override
    public String getRol() { return "cliente"; }

    @Override
    public String mostrarDatos() {
        return getNombre() + " · Cliente · reside en " + ciudadResidencia;
    }

    public String getCiudadResidencia() { return ciudadResidencia; }
    public double getCalificacion() { return calificacion; }
}
