package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.InmuebleDAO;
import com.rentainmuebles.datos.ReporteDAO;
import com.rentainmuebles.datos.UsuarioDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Administrador (hereda de Usuario).
 * aprobarInmueble() y suspenderCuenta() del diagrama NO se implementan:
 * la base no tiene columnas de estado en inmueble ni en usuario.
 */
public class Administrador extends Usuario {

    private String cargo;
    private int nivelAcceso;        // en la base es un entero de 1 a 3 (regla CHECK)

    public Administrador(int id, String nombre, String telefono, String email, String contrasena,
                         String cargo, int nivelAcceso) {
        super(id, nombre, telefono, email, contrasena);
        this.cargo = cargo;
        this.nivelAcceso = nivelAcceso;
    }

    /** CU-08 (parte de búsqueda): cuentas que coinciden con un nombre o email. */
    public List<Usuario> buscarCuentas(String texto) throws SQLException {
        return new UsuarioDAO().buscar(texto);
    }

    /** Retira un inmueble de la plataforma (reemplaza a aprobarInmueble mientras no haya estado). */
    public boolean retirarInmueble(int idInmueble) throws SQLException {
        return new InmuebleDAO().retirarComoAdmin(idInmueble);
    }

    /**
     * CU-09: reporte agrupado del periodo. tipo: "ciudad" (rentas por ciudad),
     * "ingresos" (valor por ciudad) o "inmuebles" (inmuebles más rentados).
     */
    public Map<String, Long> consultarReportes(String tipo, LocalDate desde, LocalDate hasta) throws SQLException {
        return new ReporteDAO().agrupar(tipo, desde, hasta);
    }

    @Override
    public String getRol() { return "admin"; }

    @Override
    public String mostrarDatos() {
        return getNombre() + " · Administrador (" + cargo + ", nivel " + nivelAcceso + ")";
    }

    public String getCargo() { return cargo; }
    public int getNivelAcceso() { return nivelAcceso; }
}
