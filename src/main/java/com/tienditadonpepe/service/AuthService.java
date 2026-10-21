package com.tienditadonpepe.service;

import com.tienditadonpepe.dao.IUsuarioDAO;
import com.tienditadonpepe.dao.UsuarioDAO;
import com.tienditadonpepe.model.Usuario;
import com.tienditadonpepe.util.DatosException;
import com.tienditadonpepe.util.NegocioException;
import com.tienditadonpepe.util.Passwords;

import java.util.HashMap;
import java.util.Map;

/** RF01 – Inicio de sesión con usuario y contraseña, con bloqueo temporal tras intentos fallidos. */
public class AuthService extends ServicioBase {

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MS = 5 * 60 * 1000L;

    private static final class Intentos {
        int fallidos;
        long bloqueadoHasta;
    }

    private static final Map<String, Intentos> REGISTRO = new HashMap<>();

    private final IUsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario iniciarSesion(String login, String contrasena) {
        String clave = login == null ? "" : login.trim().toLowerCase();
        if (clave.isEmpty()) throw new NegocioException("Ingresa tu usuario.");
        if (contrasena == null || contrasena.isEmpty()) throw new NegocioException("Ingresa tu contraseña.");

        verificarBloqueo(clave);
        Usuario u;
        try {
            u = usuarioDAO.readByLogin(clave);
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
        boolean correcto = u != null && Passwords.verificar(contrasena, u.getContrasena_hash());
        if (!correcto) {
            registrarFallo(clave);
            throw new NegocioException("Usuario o contraseña incorrectos.");
        }
        if (!u.estaActivo()) {
            throw new NegocioException("Tu usuario está inactivo. Comunícate con el administrador.");
        }
        synchronized (REGISTRO) {
            REGISTRO.remove(clave);
        }
        sesion.iniciar(u);
        return u;
    }

    public void cerrarSesion() {
        sesion.cerrar();
    }

    public void cambiarContrasena(String actual, String nueva, String confirmacion) {
        Usuario u = sesion.exigir();
        if (actual == null || actual.isEmpty()) throw new NegocioException("Ingresa tu contraseña actual.");
        Usuario bd;
        try {
            bd = usuarioDAO.read(u.getId_usuario());
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
        if (bd == null || !Passwords.verificar(actual, bd.getContrasena_hash())) {
            throw new NegocioException("La contraseña actual no es correcta.");
        }
        String motivo = Passwords.validarPolitica(nueva);
        if (motivo != null) throw new NegocioException(motivo);
        if (!nueva.equals(confirmacion)) throw new NegocioException("La confirmación no coincide con la nueva contraseña.");
        if (nueva.equals(actual)) throw new NegocioException("La nueva contraseña debe ser distinta de la actual.");
        try {
            String hash = Passwords.cifrar(nueva);
            usuarioDAO.updateContrasena(u.getId_usuario(), hash);
            u.setContrasena_hash(hash);
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    private void verificarBloqueo(String clave) {
        synchronized (REGISTRO) {
            Intentos i = REGISTRO.get(clave);
            if (i != null && i.bloqueadoHasta > System.currentTimeMillis()) {
                long min = Math.max(1, (i.bloqueadoHasta - System.currentTimeMillis() + 59_999) / 60_000);
                throw new NegocioException("Demasiados intentos fallidos. Vuelve a intentarlo en " + min
                        + (min == 1 ? " minuto." : " minutos."));
            }
        }
    }

    private void registrarFallo(String clave) {
        synchronized (REGISTRO) {
            Intentos i = REGISTRO.computeIfAbsent(clave, k -> new Intentos());
            if (i.bloqueadoHasta != 0 && i.bloqueadoHasta <= System.currentTimeMillis()) {
                i.fallidos = 0;
                i.bloqueadoHasta = 0;
            }
            i.fallidos++;
            if (i.fallidos >= MAX_INTENTOS) {
                i.bloqueadoHasta = System.currentTimeMillis() + BLOQUEO_MS;
                i.fallidos = 0;
            }
        }
    }
}
