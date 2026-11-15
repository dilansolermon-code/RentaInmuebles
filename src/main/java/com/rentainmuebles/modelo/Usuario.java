package com.rentainmuebles.modelo;

import com.rentainmuebles.util.Seguridad;

/**
 * Clase ABSTRACTA Usuario (diagrama de clases): reúne lo común a
 * Propietario, Cliente y Administrador. No se crean objetos "Usuario"
 * directamente, sino de alguna de sus clases hijas.
 */
public abstract class Usuario {

    private int id;
    private String nombre;
    private String telefono;
    private String email;
    private String contrasena;      // huella cifrada, tal como está en la base

    // Solo para el listado del administrador: todos los roles, ej. "Propietario, Cliente"
    private String descripcionRoles;

    protected Usuario(int id, String nombre, String telefono, String email, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.contrasena = contrasena;
    }

    /**
     * Iniciar sesión (diagrama de clases): true si el email coincide y la contraseña
     * escrita produce la misma huella que la guardada.
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

    /** Cada hija dice cuál es su rol: "cliente", "propietario" o "admin". */
    public abstract String getRol();

    /** Cada hija muestra sus datos a su manera (polimorfismo). */
    public abstract String mostrarDatos();

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }

    public String getDescripcionRoles() { return descripcionRoles; }
    public void setDescripcionRoles(String descripcionRoles) { this.descripcionRoles = descripcionRoles; }
}
