package com.tienditadonpepe.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class Passwords {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int ITERACIONES = 100_000;
    private static final int LONGITUD_SAL = 16;
    private static final int LONGITUD_HASH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Passwords() {
    }

    public static String cifrar(String contrasena) {
        byte[] sal = new byte[LONGITUD_SAL];
        RANDOM.nextBytes(sal);
        byte[] hash = derivar(contrasena, sal, ITERACIONES, LONGITUD_HASH);
        return PREFIJO + "$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String contrasena, String almacenado) {
        if (contrasena == null || almacenado == null) return false;
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(contrasena, sal, iteraciones, esperado.length);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String contrasena, byte[] sal, int iteraciones, int longitud) {
        PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, longitud * 8);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Mínimo 8 caracteres con minúscula, mayúscula, dígito y símbolo. Devuelve el motivo o null. */
    public static String validarPolitica(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (contrasena.chars().noneMatch(Character::isLowerCase)) {
            return "La contraseña debe incluir al menos una letra minúscula.";
        }
        if (contrasena.chars().noneMatch(Character::isUpperCase)) {
            return "La contraseña debe incluir al menos una letra mayúscula.";
        }
        if (contrasena.chars().noneMatch(Character::isDigit)) {
            return "La contraseña debe incluir al menos un dígito.";
        }
        if (contrasena.chars().allMatch(Character::isLetterOrDigit)) {
            return "La contraseña debe incluir al menos un símbolo especial (por ejemplo @ # $ % !).";
        }
        return null;
    }
}
