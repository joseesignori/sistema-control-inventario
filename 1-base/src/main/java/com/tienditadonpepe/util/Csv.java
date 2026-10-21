package com.tienditadonpepe.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Exportación CSV compatible con Excel en configuración regional hispana (separador «;», UTF-8 con BOM). */
public final class Csv {

    private Csv() {
    }

    public static void escribir(Path destino, String[] encabezados, List<String[]> filas) throws IOException {
        try (Writer w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write('﻿');
            linea(w, encabezados);
            for (String[] f : filas) linea(w, f);
        }
    }

    private static void linea(Writer w, String[] celdas) throws IOException {
        for (int i = 0; i < celdas.length; i++) {
            if (i > 0) w.write(';');
            w.write(escapar(celdas[i]));
        }
        w.write("\r\n");
    }

    private static String escapar(String v) {
        if (v == null) return "";
        // evita inyección de fórmulas al abrir en hojas de cálculo
        if (!v.isEmpty() && "=+@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
