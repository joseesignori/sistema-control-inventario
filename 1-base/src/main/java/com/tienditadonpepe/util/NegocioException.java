package com.tienditadonpepe.util;

/**
 * Se lanza cuando se incumple una regla del negocio (stock insuficiente, datos inválidos,
 * permisos, etc.). El mensaje está redactado para mostrarse directamente al usuario.
 */
public class NegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
