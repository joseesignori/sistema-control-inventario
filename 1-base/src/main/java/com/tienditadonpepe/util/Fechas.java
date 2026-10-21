package com.tienditadonpepe.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Formatos de fecha y hora (dd/MM/yyyy) usados en toda la interfaz. */
public final class Fechas {

    public static final Locale ES_PE = new Locale("es", "PE");
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ES_PE);

    private Fechas() {
    }

    public static String fecha(LocalDate f) {
        return f == null ? "—" : FECHA.format(f);
    }

    public static String fechaHora(LocalDateTime f) {
        return f == null ? "—" : FECHA_HORA.format(f);
    }

    /** Ejemplo: "lunes, 19 de octubre de 2026" */
    public static String fechaLarga(LocalDate f) {
        String dia = f.getDayOfWeek().getDisplayName(TextStyle.FULL, ES_PE);
        return dia + ", " + FECHA_LARGA.format(f);
    }

    /** Ejemplo: "octubre 2026" */
    public static String mesAnio(java.time.YearMonth ym) {
        return ym.getMonth().getDisplayName(TextStyle.FULL, ES_PE) + " " + ym.getYear();
    }
}
