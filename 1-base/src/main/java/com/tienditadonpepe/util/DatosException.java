package com.tienditadonpepe.util;

import java.sql.SQLException;

/**
 * Error de acceso a datos (capa DAO). Envuelve la {@link SQLException} original y la clasifica
 * para que la capa de servicios pueda traducirla a un mensaje de negocio.
 */
public class DatosException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public enum Tipo { DUPLICADO, REFERENCIADO, SIN_REFERENCIA, RESTRICCION, CONEXION, OTRO }

    private final Tipo tipo;

    public DatosException(String mensaje, SQLException causa) {
        super(mensaje, causa);
        this.tipo = clasificar(causa);
    }

    public DatosException(String mensaje) {
        super(mensaje);
        this.tipo = Tipo.OTRO;
    }

    public Tipo getTipo() {
        return tipo;
    }

    private static Tipo clasificar(SQLException e) {
        if (e == null) return Tipo.OTRO;
        int code = e.getErrorCode();
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (code == 1062) return Tipo.DUPLICADO;                       // Duplicate entry
        if (code == 1451) return Tipo.REFERENCIADO;                    // No se puede eliminar: tiene registros hijos
        if (code == 1452) return Tipo.SIN_REFERENCIA;                  // Clave foránea inexistente
        if (code == 3819 || code == 4025 || code == 4022) return Tipo.RESTRICCION; // CHECK (MySQL / MariaDB)
        if (state.startsWith("08")) return Tipo.CONEXION;
        return Tipo.OTRO;
    }
}
