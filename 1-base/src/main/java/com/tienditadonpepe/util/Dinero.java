package com.tienditadonpepe.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Utilidades para importes en soles (S/). Los importes se manejan con 2 decimales. */
public final class Dinero {

    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.US);

    private Dinero() {
    }

    /** Redondea a 2 decimales (mitad hacia arriba) evitando errores de punto flotante. */
    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static BigDecimal aBigDecimal(double valor) {
        return BigDecimal.valueOf(redondear(valor)).setScale(2, RoundingMode.HALF_UP);
    }

    /** Ejemplo: 18.3 -&gt; "S/ 18.30" */
    public static String soles(double valor) {
        return "S/ " + new DecimalFormat("#,##0.00", SIMBOLOS).format(redondear(valor));
    }

    /** Ejemplo: 18.3 -&gt; "18.30" */
    public static String numero(double valor) {
        return new DecimalFormat("0.00", SIMBOLOS).format(redondear(valor));
    }
}
