package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.InmuebleDAO;
import com.rentainmuebles.datos.RentaDAO;
import java.sql.SQLException;
import java.util.List;

/** Propietario (hereda de Usuario): registra, actualiza, retira y consulta sus inmuebles. */
public class Propietario extends Usuario {

    private String tipoPersona;     // "Natural" o "Juridica" (regla CHECK de la base)
    private double calificacion;

    public Propietario(int id, String nombre, String telefono, String email, String contrasena,
                       String tipoPersona, double calificacion) {
        super(id, nombre, telefono, email, contrasena);
        this.tipoPersona = tipoPersona;
        this.calificacion = calificacion;
    }

    /** CU-05: registra un inmueble asociado a este propietario y a una ciudad. */
    public Inmueble registrarInmueble(String direccion, double area, int habitaciones,
                                      int capacidad, double precio, int idCiudad) throws SQLException {
        int idInmueble = new InmuebleDAO().registrar(getId(), direccion, area, habitaciones, capacidad, idCiudad, precio);
        return new InmuebleDAO().buscarDelPropietario(getId(), idInmueble);
    }

    /** CU-05: retira (deja de publicar) uno de sus inmuebles. */
    public boolean retirarInmueble(int idInmueble) throws SQLException {
        return new InmuebleDAO().retirar(getId(), idInmueble);
    }

    /** «include» Actualizar datos: cambia los datos y el precio de un inmueble propio. */
    public boolean actualizarDatos(int idInmueble, String direccion, double area, int habitaciones,
                                   int capacidad, double precio, int idCiudad) throws SQLException {
        return new InmuebleDAO().actualizar(getId(), idInmueble, direccion, area, habitaciones, capacidad, idCiudad, precio);
    }

    /** CU-06: inmuebles publicados por este propietario. */
    public List<Inmueble> consultarInmueblesPropios() throws SQLException {
        return new InmuebleDAO().listarPorPropietario(getId());
    }

    /** Un inmueble propio por id (null si no existe o es de otro propietario). */
    public Inmueble buscarInmueblePropio(int idInmueble) throws SQLException {
        return new InmuebleDAO().buscarDelPropietario(getId(), idInmueble);
    }

    /** CU-06 «Ver reservas»: reservas de un inmueble propio (0 = de todos sus inmuebles). */
    public List<Renta> consultarReservas(int idInmueble) throws SQLException {
        return new RentaDAO().listarPorPropietario(getId(), idInmueble);
    }

    @Override
    public String getRol() { return "propietario"; }

    @Override
    public String mostrarDatos() {
        return getNombre() + " · Propietario (persona " + tipoPersona + ")";
    }

    public String getTipoPersona() { return tipoPersona; }
    public double getCalificacion() { return calificacion; }
}
