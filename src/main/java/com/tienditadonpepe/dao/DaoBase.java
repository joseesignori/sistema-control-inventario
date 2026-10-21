package com.tienditadonpepe.dao;

import com.tienditadonpepe.config.ConexionBD;
import com.tienditadonpepe.util.DatosException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Utilidades comunes para los DAO JDBC. */
abstract class DaoBase {

    @FunctionalInterface
    protected interface SqlFn<R> {
        R aplicar(Connection c) throws SQLException;
    }

    /** Ejecuta la operación con la conexión del Singleton y traduce los errores SQL. */
    protected <R> R ejecutar(SqlFn<R> fn) {
        try {
            return fn.aplicar(ConexionBD.getInstance().getConnection());
        } catch (SQLException e) {
            throw new DatosException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    protected static LocalDate fecha(ResultSet rs, String col) throws SQLException {
        return rs.getObject(col, LocalDate.class);
    }

    protected static LocalDateTime fechaHora(ResultSet rs, String col) throws SQLException {
        return rs.getObject(col, LocalDateTime.class);
    }

    protected static double importe(ResultSet rs, String col) throws SQLException {
        java.math.BigDecimal v = rs.getBigDecimal(col);
        return v == null ? 0 : v.doubleValue();
    }

    protected static Integer entero(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    protected static void setEntero(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.INTEGER);
        else ps.setInt(idx, v);
    }

    protected static void setTexto(PreparedStatement ps, int idx, String v) throws SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.VARCHAR);
        else ps.setString(idx, v);
    }
}
