package com.tienditadonpepe.util;

import java.util.regex.Pattern;

/** Validaciones de campos de formularios. Cada método lanza {@link NegocioException} con un mensaje claro. */
public final class Validaciones {

    private static final Pattern SOLO_DIGITOS = Pattern.compile("\\d+");
    private static final Pattern TELEFONO = Pattern.compile("[0-9+() \\-]{6,20}");
    private static final Pattern CORREO = Pattern.compile("^[\\w.%+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+$");

    private Validaciones() {
    }

    public static String texto(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s{2,}", " ");
    }

    public static String requerido(String valor, String campo, int max) {
        String v = texto(valor);
        if (v.isEmpty()) throw new NegocioException("Ingresa " + campo + ".");
        if (v.length() > max) throw new NegocioException(campo.substring(0, 1).toUpperCase() + campo.substring(1)
                + " no puede superar " + max + " caracteres.");
        return v;
    }

    public static String opcional(String valor, String campo, int max) {
        String v = texto(valor);
        if (v.length() > max) throw new NegocioException(campo.substring(0, 1).toUpperCase() + campo.substring(1)
                + " no puede superar " + max + " caracteres.");
        return v.isEmpty() ? null : v;
    }

    public static String telefonoOpcional(String valor) {
        String v = opcional(valor, "el teléfono", 20);
        if (v != null && !TELEFONO.matcher(v).matches()) {
            throw new NegocioException("El teléfono solo puede contener números, espacios y los símbolos + - ( ).");
        }
        return v;
    }

    public static String correoOpcional(String valor) {
        String v = opcional(valor, "el correo", 100);
        if (v != null && !CORREO.matcher(v).matches()) {
            throw new NegocioException("El correo electrónico no tiene un formato válido.");
        }
        return v;
    }

    public static String ruc(String valor) {
        String v = texto(valor);
        if (v.length() != 11 || !SOLO_DIGITOS.matcher(v).matches()) {
            throw new NegocioException("El RUC debe tener exactamente 11 dígitos.");
        }
        return v;
    }

    /** DNI opcional: vacío = null; si se escribe debe tener exactamente 8 dígitos. */
    public static String dniOpcional(String valor) {
        String v = texto(valor);
        if (v.isEmpty()) return null;
        if (v.length() != 8 || !SOLO_DIGITOS.matcher(v).matches()) {
            throw new NegocioException("El DNI debe tener exactamente 8 dígitos.");
        }
        return v;
    }

    /** RUC opcional: vacío = null; si se escribe debe tener exactamente 11 dígitos. */
    public static String rucOpcional(String valor) {
        String v = texto(valor);
        return v.isEmpty() ? null : ruc(v);
    }

    /** DNI (8 dígitos) o RUC (11 dígitos). */
    public static String dniORuc(String valor) {
        String v = texto(valor);
        if (!SOLO_DIGITOS.matcher(v).matches() || (v.length() != 8 && v.length() != 11)) {
            throw new NegocioException("Ingresa un DNI (8 dígitos) o un RUC (11 dígitos) válido.");
        }
        return v;
    }

    public static int enteroPositivo(String valor, String campo) {
        try {
            int n = Integer.parseInt(texto(valor));
            if (n <= 0) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            throw new NegocioException("La " + campo + " debe ser un número entero mayor que cero.");
        }
    }

    public static int enteroNoNegativo(String valor, String campo) {
        try {
            int n = Integer.parseInt(texto(valor));
            if (n < 0) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            throw new NegocioException("El " + campo + " debe ser un número entero (0 o mayor).");
        }
    }

    public static double importe(String valor, String campo) {
        try {
            double d = Double.parseDouble(texto(valor).replace(',', '.'));
            if (d < 0 || Double.isNaN(d) || Double.isInfinite(d)) throw new NumberFormatException();
            return Dinero.redondear(d);
        } catch (NumberFormatException e) {
            throw new NegocioException("El " + campo + " debe ser un importe válido (por ejemplo 4.50).");
        }
    }
}
