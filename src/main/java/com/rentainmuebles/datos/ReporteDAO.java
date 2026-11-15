package com.rentainmuebles.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cifras para los reportes del administrador (CU-09) y los indicadores del propietario. */
public class ReporteDAO {

    // Rentas cuya fecha de entrada cae dentro del periodo [desde, hasta]
    private static final String EN_PERIODO = " r.fecha_entrada BETWEEN ? AND ? ";

    // ======================= ADMINISTRADOR (CU-09) =======================

    public long totalUsuarios() throws SQLException {
        return numero("SELECT COUNT(*) FROM usuario");
    }

    public long totalInmueblesPublicados() throws SQLException {
        return numero("SELECT COUNT(DISTINCT id_inmueble) FROM propietario_inmueble");
    }

    /** Rentas activas del periodo. */
    public long rentasActivas(LocalDate desde, LocalDate hasta) throws SQLException {
        return numero("SELECT COUNT(*) FROM renta r WHERE r.estado = 'activo' AND" + EN_PERIODO, desde, hasta);
    }

    /** Rentas canceladas (inactivas) del periodo. */
    public long rentasCanceladas(LocalDate desde, LocalDate hasta) throws SQLException {
        return numero("SELECT COUNT(*) FROM renta r WHERE r.estado = 'inactivo' AND" + EN_PERIODO, desde, hasta);
    }

    /** Suma del valor de las rentas activas del periodo. */
    public long valorRentasActivas(LocalDate desde, LocalDate hasta) throws SQLException {
        return numero("SELECT COALESCE(SUM(r.valor), 0)::bigint FROM renta r WHERE r.estado = 'activo' AND" + EN_PERIODO, desde, hasta);
    }

    /**
     * Agrupa las rentas activas del periodo según el tipo de reporte:
     *  "ciudad"    -> número de rentas por ciudad
     *  "ingresos"  -> valor total por ciudad
     *  "inmuebles" -> los 10 inmuebles con más rentas
     * LinkedHashMap guarda los pares nombre -> cantidad en el orden en que llegan (de mayor a menor).
     */
    public Map<String, Long> agrupar(String tipo, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql;
        if ("ingresos".equals(tipo)) {
            sql = "SELECT c.nombre AS grupo, COALESCE(SUM(r.valor), 0)::bigint AS total "
                + "FROM renta r JOIN inmueble i ON i.id = r.id_inmueble JOIN ciudad c ON c.id = i.id_ciudad "
                + "WHERE r.estado = 'activo' AND" + EN_PERIODO + "GROUP BY c.nombre ORDER BY total DESC LIMIT 10";
        } else if ("inmuebles".equals(tipo)) {
            sql = "SELECT i.direccion AS grupo, COUNT(*) AS total "
                + "FROM renta r JOIN inmueble i ON i.id = r.id_inmueble "
                + "WHERE r.estado = 'activo' AND" + EN_PERIODO + "GROUP BY i.direccion ORDER BY total DESC LIMIT 10";
        } else {   // "ciudad" (valor por defecto)
            sql = "SELECT c.nombre AS grupo, COUNT(*) AS total "
                + "FROM renta r JOIN inmueble i ON i.id = r.id_inmueble JOIN ciudad c ON c.id = i.id_ciudad "
                + "WHERE r.estado = 'activo' AND" + EN_PERIODO + "GROUP BY c.nombre ORDER BY total DESC LIMIT 10";
        }
        Map<String, Long> resultado = new LinkedHashMap<>();
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString("grupo"), rs.getLong("total"));
                }
            }
        }
        return resultado;
    }

    // ======================= PROPIETARIO =======================

    /** Rentas activas de los inmuebles de un propietario. */
    public long rentasActivasDePropietario(int idPropietario) throws SQLException {
        return numero("SELECT COUNT(*) FROM renta r JOIN propietario_inmueble pi ON pi.id_inmueble = r.id_inmueble "
                    + "WHERE r.estado = 'activo' AND pi.id_propietario = ?", idPropietario);
    }

    /** Ingresos (suma del valor) de las rentas activas de un propietario. */
    public long ingresosDePropietario(int idPropietario) throws SQLException {
        return numero("SELECT COALESCE(SUM(r.valor), 0)::bigint FROM renta r JOIN propietario_inmueble pi ON pi.id_inmueble = r.id_inmueble "
                    + "WHERE r.estado = 'activo' AND pi.id_propietario = ?", idPropietario);
    }

    // ======================= APOYO =======================

    /**
     * Ejecuta una consulta que devuelve un solo número.
     * Los "parametros" reemplazan los ? en orden; pueden ser enteros o fechas.
     */
    private long numero(String sql, Object... parametros) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                if (parametros[i] instanceof LocalDate) {
                    ps.setDate(i + 1, Date.valueOf((LocalDate) parametros[i]));   // fecha
                } else {
                    ps.setInt(i + 1, (Integer) parametros[i]);                     // número entero
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
