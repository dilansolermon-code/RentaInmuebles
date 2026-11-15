package com.rentainmuebles.modelo;

/** Una ciudad del catálogo (tabla ciudad). */
public class Ciudad {

    private int id;
    private String nombre;

    public Ciudad(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
}
