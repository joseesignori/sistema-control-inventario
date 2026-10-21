package com.tienditadonpepe.service;

import com.tienditadonpepe.model.Usuario;
import com.tienditadonpepe.util.NegocioException;

/** Sesión activa de la aplicación (un solo usuario autenticado a la vez). */
public final class SesionUsuario {

    private static final SesionUsuario INSTANCIA = new SesionUsuario();

    private Usuario usuario;

    private SesionUsuario() {
    }

    public static SesionUsuario getInstance() {
        return INSTANCIA;
    }

    public synchronized void iniciar(Usuario u) {
        this.usuario = u;
    }

    public synchronized void cerrar() {
        this.usuario = null;
    }

    public synchronized boolean haySesion() {
        return usuario != null;
    }

    public synchronized Usuario getUsuario() {
        return usuario;
    }

    /** Devuelve el usuario autenticado o falla si no hay sesión. */
    public synchronized Usuario exigir() {
        if (usuario == null) throw new NegocioException("Tu sesión no está iniciada. Inicia sesión para continuar.");
        return usuario;
    }

    public synchronized Usuario exigirAdministrador() {
        Usuario u = exigir();
        if (!u.esAdministrador()) throw new NegocioException("Esta operación está reservada para el administrador.");
        return u;
    }
}
