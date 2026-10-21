package com.tienditadonpepe.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Prepara la base de datos en el primer arranque: si el esquema no existe lo crea y carga los
 * datos iniciales (scripts en /db). Así el sistema funciona con solo tener MySQL (XAMPP) encendido.
 * Los mismos scripts pueden importarse manualmente desde phpMyAdmin.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    /** Resultado de la preparación. */
    public static final class Resultado {
        public final boolean ok;
        public final boolean seCreo;
        public final String mensaje;

        Resultado(boolean ok, boolean seCreo, String mensaje) {
            this.ok = ok;
            this.seCreo = seCreo;
            this.mensaje = mensaje;
        }
    }

    public static Resultado asegurarBaseDeDatos() {
        try (Connection c = DriverManager.getConnection(AppConfig.urlServidor(), AppConfig.dbUser(), AppConfig.dbPassword())) {
            if (existeEsquema(c)) {
                migrar(c);
                return new Resultado(true, false, "Base de datos lista.");
            }
            ejecutarScript(c, "/db/01_schema.sql");
            ejecutarScript(c, "/db/02_datos_iniciales.sql");
            if (AppConfig.cargarDemo()) {
                ejecutarScript(c, "/db/03_datos_demo.sql");
            }
            return new Resultado(true, true, "Base de datos creada correctamente.");
        } catch (SQLException e) {
            return new Resultado(false, false, mensajeAmigable(e));
        } catch (IOException e) {
            return new Resultado(false, false, "No se pudieron leer los scripts de la base de datos: " + e.getMessage());
        }
    }

    private static boolean existeEsquema(Connection c) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = 'usuario'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, AppConfig.dbName());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Actualiza bases creadas con una versión anterior del sistema (sin perder datos). */
    private static void migrar(Connection c) throws SQLException {
        if (!existeColumna(c, "cliente", "dni")) {
            String bd = "`" + AppConfig.dbName().replace("`", "") + "`";
            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE " + bd + ".cliente "
                        + "ADD COLUMN dni VARCHAR(8) NULL AFTER nombre_completo, "
                        + "ADD COLUMN ruc VARCHAR(11) NULL AFTER dni, "
                        + "ADD UNIQUE KEY uk_cliente_dni (dni), ADD UNIQUE KEY uk_cliente_ruc (ruc), "
                        + "ADD CONSTRAINT chk_cliente_dni CHECK (dni IS NULL OR dni REGEXP '^[0-9]{8}$'), "
                        + "ADD CONSTRAINT chk_cliente_ruc CHECK (ruc IS NULL OR ruc REGEXP '^[0-9]{11}$')");
                // Datos ficticios de los clientes de demostración originales
                st.execute("UPDATE " + bd + ".cliente SET dni = '45123789', ruc = '10451237891' WHERE nombre_completo = 'María Torres Quispe' AND dni IS NULL");
                st.execute("UPDATE " + bd + ".cliente SET dni = '41876532', ruc = '20555666777' WHERE nombre_completo = 'Jorge Villanueva Cano' AND dni IS NULL");
                st.execute("UPDATE " + bd + ".cliente SET dni = '43219876' WHERE nombre_completo = 'Rosa Huamán Effio' AND dni IS NULL");
            }
        }
    }

    private static boolean existeColumna(Connection c, String tabla, String columna) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, AppConfig.dbName());
            ps.setString(2, tabla);
            ps.setString(3, columna);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static void ejecutarScript(Connection c, String recurso) throws SQLException, IOException {
        String script;
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream(recurso)) {
            if (in == null) throw new IOException("Falta el recurso " + recurso);
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Statement st = c.createStatement()) {
            for (String sentencia : dividir(script)) {
                st.execute(sentencia);
            }
        }
    }

    /** Divide un script SQL en sentencias respetando cadenas entre comillas y comentarios "--". */
    static List<String> dividir(String script) {
        List<String> sentencias = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enCadena = false;
        int n = script.length();
        for (int i = 0; i < n; i++) {
            char ch = script.charAt(i);
            if (enCadena) {
                actual.append(ch);
                if (ch == '\\' && i + 1 < n) {
                    actual.append(script.charAt(++i));
                } else if (ch == '\'') {
                    if (i + 1 < n && script.charAt(i + 1) == '\'') {
                        actual.append(script.charAt(++i));
                    } else {
                        enCadena = false;
                    }
                }
                continue;
            }
            if (ch == '-' && i + 1 < n && script.charAt(i + 1) == '-') {
                while (i < n && script.charAt(i) != '\n') i++;
                actual.append('\n');
                continue;
            }
            if (ch == '\'') {
                enCadena = true;
                actual.append(ch);
            } else if (ch == ';') {
                String s = actual.toString().trim();
                if (!s.isEmpty()) sentencias.add(s);
                actual.setLength(0);
            } else {
                actual.append(ch);
            }
        }
        String resto = actual.toString().trim();
        if (!resto.isEmpty()) sentencias.add(resto);
        return sentencias;
    }

    private static String mensajeAmigable(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        if (estado.startsWith("08") || e.getMessage() != null && e.getMessage().toLowerCase().contains("communications link")
                || e.getMessage() != null && e.getMessage().toLowerCase().contains("connection refused")
                || e.getMessage() != null && e.getMessage().toLowerCase().contains("could not connect")) {
            return "No se pudo conectar con MySQL en " + AppConfig.dbHost() + ":" + AppConfig.dbPort()
                    + ".\nEnciende el servicio MySQL desde el panel de XAMPP y revisa db.properties.";
        }
        if (e.getErrorCode() == 1045) {
            return "MySQL rechazó el usuario o la contraseña indicados en db.properties (db.user / db.password).";
        }
        return "Error de base de datos: " + e.getMessage();
    }
}
