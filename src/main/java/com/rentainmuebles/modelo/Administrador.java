package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.InmuebleDAO;
import com.rentainmuebles.datos.ReporteDAO;
import com.rentainmuebles.datos.UsuarioDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Administrador (hereda de Usuario).
 * Implementa aprobarInmueble(), suspenderCuenta() y consultarReportes() del diagrama de clases.
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

    // ======================= CU-07 APROBAR INMUEBLE =======================

    /** Inmuebles que coinciden con un texto y un estado ("" = todos los estados). Máximo 100. */
    public List<Inmueble> consultarInmuebles(String texto, String estado) throws SQLException {
        return new InmuebleDAO().buscarComoAdmin(texto, estado);
    }

    /** Un inmueble por id, en cualquier estado (null si no existe). */
    public Inmueble buscarInmueble(int idInmueble) throws SQLException {
        return new InmuebleDAO().buscarComoAdminPorId(idInmueble);
    }

    /** Aprueba o rechaza un inmueble. nuevoEstado: Inmueble.APROBADA o Inmueble.RECHAZADO. */
    public boolean aprobarInmueble(int idInmueble, String nuevoEstado) throws SQLException {
        return new InmuebleDAO().cambiarEstadoAprobacion(idInmueble, nuevoEstado);
    }

    /** Retira un inmueble de la plataforma (queda en estado "retirado"). */
    public boolean retirarInmueble(int idInmueble) throws SQLException {
        return new InmuebleDAO().retirarComoAdmin(idInmueble);
    }

    // ======================= CU-08 SUSPENDER CUENTA =======================

    /** Cuentas que coinciden con un nombre o email. */
    public List<Usuario> buscarCuentas(String texto) throws SQLException {
        return new UsuarioDAO().buscar(texto);
    }

    /** Suspende (Usuario.INACTIVO) o restaura (Usuario.ACTIVO) la cuenta de un usuario. */
    public boolean suspenderCuenta(int idUsuario, String nuevoEstado) throws SQLException {
        return new UsuarioDAO().cambiarEstadoCuenta(idUsuario, nuevoEstado);
    }

    // ======================= CU-09 CONSULTAR REPORTES =======================

    /**
     * Genera un objeto Reporte con los totales del periodo y la tabla agrupada.
     * tipo: "ciudad" (rentas por ciudad), "ingresos" (valor por ciudad) o "inmuebles" (más rentados).
     */
    public Reporte consultarReportes(String tipo, LocalDate desde, LocalDate hasta) throws SQLException {
        ReporteDAO dao = new ReporteDAO();
        Reporte reporte = new Reporte(tipo, desde, hasta);
        reporte.setTotalUsuarios(dao.totalUsuarios());
        reporte.setInmueblesPublicados(dao.totalInmueblesPublicados());
        reporte.setRentasActivas(dao.rentasActivas(desde, hasta));
        reporte.setRentasCanceladas(dao.rentasCanceladas(desde, hasta));
        reporte.setValorRentasActivas(dao.valorRentasActivas(desde, hasta));
        reporte.setDatos(dao.agrupar(tipo, desde, hasta));
        return reporte;
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
