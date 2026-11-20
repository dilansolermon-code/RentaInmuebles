package com.rentainmuebles.modelo;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resultado de CU-09 Consultar reportes. Agrupa en un solo objeto los totales del periodo
 * y la tabla agrupada, y sabe calcular su total y el ancho de las barras.
 * Así la pantalla reportes.jsp solo muestra datos y no hace cálculos.
 */
public class Reporte {

    private String tipo;                 // "ciudad", "ingresos" o "inmuebles"
    private LocalDate desde;
    private LocalDate hasta;
    private long totalUsuarios;
    private long inmueblesPublicados;
    private long rentasActivas;
    private long rentasCanceladas;
    private long valorRentasActivas;
    private Map<String, Long> datos = new LinkedHashMap<>();   // nombre -> cantidad, de mayor a menor

    public Reporte(String tipo, LocalDate desde, LocalDate hasta) {
        this.tipo = tipo;
        this.desde = desde;
        this.hasta = hasta;
    }

    /** Título que se muestra encima de la tabla. */
    public String getTitulo() {
        if (tipo.equals("ingresos")) return "Ingresos por ciudad";
        if (tipo.equals("inmuebles")) return "Inmuebles más rentados";
        return "Rentas activas por ciudad";
    }

    /** true si la tabla muestra dinero (para poner el signo $). */
    public boolean esDinero() {
        return tipo.equals("ingresos");
    }

    /** true si no hubo rentas en el periodo. */
    public boolean estaVacio() {
        return datos.isEmpty();
    }

    /** Suma de la columna de la tabla. */
    public long getTotal() {
        long total = 0;
        for (long cantidad : datos.values()) {
            total += cantidad;
        }
        return total;
    }

    /** Porcentaje de una fila frente a la mayor (para el ancho de la barra, de 0 a 100). */
    public long porcentaje(long cantidad) {
        long maximo = 1;                                  // evita dividir por cero
        for (long c : datos.values()) {
            maximo = Math.max(maximo, c);
        }
        return cantidad * 100 / maximo;
    }

    public String getTipo() { return tipo; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public long getTotalUsuarios() { return totalUsuarios; }
    public long getInmueblesPublicados() { return inmueblesPublicados; }
    public long getRentasActivas() { return rentasActivas; }
    public long getRentasCanceladas() { return rentasCanceladas; }
    public long getValorRentasActivas() { return valorRentasActivas; }
    public Map<String, Long> getDatos() { return datos; }

    public void setTotalUsuarios(long totalUsuarios) { this.totalUsuarios = totalUsuarios; }
    public void setInmueblesPublicados(long inmueblesPublicados) { this.inmueblesPublicados = inmueblesPublicados; }
    public void setRentasActivas(long rentasActivas) { this.rentasActivas = rentasActivas; }
    public void setRentasCanceladas(long rentasCanceladas) { this.rentasCanceladas = rentasCanceladas; }
    public void setValorRentasActivas(long valorRentasActivas) { this.valorRentasActivas = valorRentasActivas; }
    public void setDatos(Map<String, Long> datos) { this.datos = datos; }
}
