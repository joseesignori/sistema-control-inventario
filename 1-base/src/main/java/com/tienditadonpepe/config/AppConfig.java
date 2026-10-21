package com.tienditadonpepe.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuración de la aplicación. Orden de prioridad:
 * propiedades del sistema (-Dclave=valor) &gt; archivo db.properties junto al proyecto &gt; valores por defecto incluidos.
 */
public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    PROPS.load(r);
                }
            }
        } catch (IOException ignored) {
            // se usan los valores por defecto
        }
        Path externo = Paths.get("db.properties");
        if (Files.isRegularFile(externo)) {
            try (Reader r = Files.newBufferedReader(externo, StandardCharsets.UTF_8)) {
                PROPS.load(r);
            } catch (IOException ignored) {
                // se usan los valores ya cargados
            }
        }
    }

    private AppConfig() {
    }

    public static String get(String clave, String porDefecto) {
        String sys = System.getProperty(clave);
        if (sys != null) return sys;
        return PROPS.getProperty(clave, porDefecto).trim();
    }

    public static int getInt(String clave, int porDefecto) {
        try {
            return Integer.parseInt(get(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    public static double getDouble(String clave, double porDefecto) {
        try {
            return Double.parseDouble(get(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    // ---- Base de datos ------------------------------------------------------
    public static String dbHost() { return get("db.host", "localhost"); }
    public static String dbPort() { return get("db.port", "3306"); }
    public static String dbName() { return get("db.name", "tiendita_don_pepe"); }
    public static String dbUser() { return get("db.user", "root"); }
    public static String dbPassword() { return get("db.password", ""); }
    public static boolean cargarDemo() { return Boolean.parseBoolean(get("db.cargar_demo", "true")); }

    private static String params() {
        return get("db.params", "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima&characterEncoding=UTF-8");
    }

    /** URL JDBC hacia el servidor, sin seleccionar esquema (sirve para crear la base de datos). */
    public static String urlServidor() {
        return "jdbc:mysql://" + dbHost() + ":" + dbPort() + "/?" + params();
    }

    /** URL JDBC hacia la base de datos del sistema. */
    public static String urlBaseDatos() {
        return "jdbc:mysql://" + dbHost() + ":" + dbPort() + "/" + dbName() + "?" + params();
    }

    // ---- Reglas de negocio ----------------------------------------------------
    public static int diasAlertaVencimiento() { return getInt("alerta.vencimiento.dias", 90); }
    public static double igv() { return getDouble("venta.igv", 0.18); }

    // ---- Datos de la tienda ---------------------------------------------------
    public static String tiendaNombre() { return get("tienda.nombre", "TIENDITA DE DON PEPE"); }
    public static String tiendaRuc() { return get("tienda.ruc", ""); }
    public static String tiendaDireccion() { return get("tienda.direccion", ""); }
    public static String tiendaTelefono() { return get("tienda.telefono", ""); }
}
