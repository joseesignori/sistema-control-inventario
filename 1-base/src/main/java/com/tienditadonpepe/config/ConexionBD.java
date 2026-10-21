package com.tienditadonpepe.config;

import com.tienditadonpepe.util.DatosException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Patrón <b>Singleton</b> (apartado 2.1.4, Figura 14): garantiza una única instancia que administra
 * la conexión compartida hacia MySQL. Todos los DAO obtienen la conexión desde aquí.
 * <p>
 * A diferencia del ejemplo mínimo del informe, la conexión se abre de forma diferida y se
 * restablece automáticamente si el servidor la cerró (RNF01: disponibilidad del sistema).
 * El acceso está sincronizado y las operaciones compuestas se ejecutan con {@link #enTransaccion}
 * para preservar la integridad de los datos (RNF05).
 */
public final class ConexionBD {

    /** Operación que se ejecuta dentro de una transacción. */
    @FunctionalInterface
    public interface Tarea<T> {
        T ejecutar() throws SQLException;
    }

    private static ConexionBD instancia;

    private final Object candado = new Object();
    private Connection con;
    private int nivelTransaccion = 0;

    private ConexionBD() {
    }

    public static synchronized ConexionBD getInstance() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        synchronized (candado) {
            if (con == null || con.isClosed() || (nivelTransaccion == 0 && !con.isValid(2))) {
                cerrarSilencioso();
                con = DriverManager.getConnection(AppConfig.urlBaseDatos(), AppConfig.dbUser(), AppConfig.dbPassword());
                con.setAutoCommit(true);
                nivelTransaccion = 0;
            }
            return con;
        }
    }

    /**
     * Ejecuta la tarea en una transacción (commit si termina bien, rollback si falla).
     * Admite anidamiento: solo la transacción más externa confirma o revierte.
     */
    public <T> T enTransaccion(Tarea<T> tarea) {
        synchronized (candado) {
            Connection c;
            try {
                c = getConnection();
            } catch (SQLException e) {
                throw new DatosException("No se pudo conectar con la base de datos.", e);
            }
            boolean externa = nivelTransaccion == 0;
            try {
                if (externa) c.setAutoCommit(false);
                nivelTransaccion++;
                T resultado = tarea.ejecutar();
                if (externa) c.commit();
                return resultado;
            } catch (SQLException e) {
                if (externa) rollbackSilencioso(c);
                throw new DatosException("Error al ejecutar la operación en la base de datos.", e);
            } catch (RuntimeException e) {
                if (externa) rollbackSilencioso(c);
                throw e;
            } finally {
                nivelTransaccion--;
                if (externa) {
                    try {
                        c.setAutoCommit(true);
                    } catch (SQLException ignored) {
                        // la conexión se restablecerá en la próxima solicitud
                    }
                }
            }
        }
    }

    /** Verifica que se pueda conectar; devuelve null si todo está bien o el mensaje del error. */
    public String probarConexion() {
        try {
            Connection c = getConnection();
            return c.isValid(3) ? null : "La conexión con la base de datos no responde.";
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    public void cerrar() {
        synchronized (candado) {
            cerrarSilencioso();
        }
    }

    private void cerrarSilencioso() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
                // nada que hacer
            }
            con = null;
        }
    }

    private static void rollbackSilencioso(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // la transacción ya no es válida
        }
    }
}
