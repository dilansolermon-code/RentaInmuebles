package com.rentainmuebles.modelo;

import com.rentainmuebles.datos.UsuarioDAO;
import com.rentainmuebles.util.Seguridad;
import java.sql.SQLException;
import java.util.List;

/**
 * Clase ABSTRACTA Usuario (diagrama de clases): reúne lo común a
 * Propietario, Cliente y Administrador. No se crean objetos "Usuario"
 * directamente, sino de alguna de sus clases hijas.
 *
 * Regla de POO del proyecto: las pantallas (JSP) y los servlets le piden las cosas
 * a los objetos; solo los objetos hablan con la base de datos (a través de los DAO).
 */
public abstract class Usuario {

    // Valores posibles de estado_cuenta (constantes: se escriben una sola vez)
    public static final String ACTIVO = "activo";
    public static final String INACTIVO = "inactivo";

    private int id;
    private String nombre;
    private String telefono;
    private String email;
    private String contrasena;      // huella cifrada, tal como está en la base
    private String estadoCuenta = ACTIVO;

    // Solo para el listado del administrador: todos los roles, ej. "Propietario, Cliente"
    private String descripcionRoles;

    protected Usuario(int id, String nombre, String telefono, String email, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.contrasena = contrasena;
    }

    // ======================= MÉTODOS DE CLASE (static) =======================
    // Se usan cuando todavía no hay un objeto Usuario, por ejemplo antes de iniciar sesión.

    /** Todas las cuentas (una por rol) que tiene un correo. */
    public static List<Usuario> buscarPorEmail(String email) throws SQLException {
        return new UsuarioDAO().buscarRolesPorEmail(email);
    }

    /** true si ya existe un usuario con ese correo. */
    public static boolean emailExiste(String email) throws SQLException {
        return new UsuarioDAO().emailExiste(email);
    }

    /** CU-01 Registrarse: crea el usuario con su rol ("cliente" o "propietario") y su cuenta bancaria. */
    public static void registrar(String tipo, String nombre, String telefono, String email, String contrasena,
                                 int idCiudad, String tipoPersona,
                                 int idBanco, String numeroCta, String tipoCta) throws SQLException {
        new UsuarioDAO().registrar(tipo, nombre, telefono, email, contrasena,
                                   idCiudad, tipoPersona, idBanco, numeroCta, tipoCta);
    }

    // ======================= MÉTODOS DEL OBJETO =======================

    /**
     * Iniciar sesión (diagrama de clases): true si el email coincide y la contraseña
     * escrita produce la misma huella que la guardada.
     * No revisa si la cuenta está suspendida; eso lo hace estaActiva().
     */
    public boolean iniciarSesion(String email, String contrasena) {
        if (email == null || contrasena == null || this.contrasena == null) {
            return false;
        }
        return this.email.equalsIgnoreCase(email.trim())
                && this.contrasena.equals(Seguridad.cifrar(contrasena));
    }

    /** Cerrar sesión (diagrama de clases): borra de la memoria la huella de la contraseña. */
    public void cerrarSesion() {
        this.contrasena = null;
    }

    /** true si la cuenta puede usarse (no está suspendida por el administrador). */
    public boolean estaActiva() {
        return ACTIVO.equals(estadoCuenta);
    }

    /** Agrega otro rol (cliente o propietario) a esta misma cuenta, con su cuenta bancaria. */
    public void agregarRol(String tipo, int idCiudad, String tipoPersona,
                           int idBanco, String numeroCta, String tipoCta) throws SQLException {
        new UsuarioDAO().agregarRol(id, tipo, idCiudad, tipoPersona, idBanco, numeroCta, tipoCta);
    }

    /** Mi perfil: datos de contacto y cuenta bancaria del rol con el que entró. */
    public Perfil consultarPerfil() throws SQLException {
        return new UsuarioDAO().obtenerPerfil(id, getRol());
    }

    /**
     * Mi perfil: guarda los datos en la base y también en este objeto
     * (así el nombre nuevo se ve de inmediato en el menú). numeroCta null = conserva el actual.
     */
    public void actualizarPerfil(String nombre, String telefono, String email,
                                 int idBanco, String numeroCta, String tipoCta) throws SQLException {
        new UsuarioDAO().actualizarPerfil(id, getRol(), nombre, telefono, email, idBanco, numeroCta, tipoCta);
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    /** Cada hija dice cuál es su rol: "cliente", "propietario" o "admin". */
    public abstract String getRol();

    /** Cada hija muestra sus datos a su manera (polimorfismo). */
    public abstract String mostrarDatos();

    /** Calificación para el listado del administrador. Las hijas que tienen calificación la sobrescriben. */
    public String getCalificacionTexto() {
        return "—";
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }

    public String getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(String estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public String getDescripcionRoles() { return descripcionRoles; }
    public void setDescripcionRoles(String descripcionRoles) { this.descripcionRoles = descripcionRoles; }
}
