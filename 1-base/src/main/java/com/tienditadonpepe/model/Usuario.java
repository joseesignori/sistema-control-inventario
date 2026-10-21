package com.tienditadonpepe.model;

/** Personal autorizado a usar el sistema (Administrador o Cajero). Ver Figura 32 y Tabla 21. */
public class Usuario {

    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_CAJERO = "Cajero";
    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_INACTIVO = "Inactivo";

    private int id_usuario;
    private String nombre_completo;
    private String usuario_login;
    private String contrasena_hash;
    private String rol;
    private String estado = ESTADO_ACTIVO;

    public Usuario() {
    }

    public Usuario(int id_usuario, String nombre_completo, String usuario_login, String contrasena_hash, String rol, String estado) {
        this.id_usuario = id_usuario;
        this.nombre_completo = nombre_completo;
        this.usuario_login = usuario_login;
        this.contrasena_hash = contrasena_hash;
        this.rol = rol;
        this.estado = estado;
    }

    public boolean esAdministrador() {
        return ROL_ADMINISTRADOR.equals(rol);
    }

    public boolean estaActivo() {
        return ESTADO_ACTIVO.equals(estado);
    }

    /** Iniciales para el avatar (por ejemplo "JP"). */
    public String iniciales() {
        if (nombre_completo == null || nombre_completo.isBlank()) return "?";
        String[] p = nombre_completo.trim().split("\\s+");
        String a = p[0].substring(0, 1);
        String b = p.length > 1 ? p[1].substring(0, 1) : "";
        return (a + b).toUpperCase();
    }

    public int getId_usuario() { return id_usuario; }
    public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }
    public String getNombre_completo() { return nombre_completo; }
    public void setNombre_completo(String nombre_completo) { this.nombre_completo = nombre_completo; }
    public String getUsuario_login() { return usuario_login; }
    public void setUsuario_login(String usuario_login) { this.usuario_login = usuario_login; }
    public String getContrasena_hash() { return contrasena_hash; }
    public void setContrasena_hash(String contrasena_hash) { this.contrasena_hash = contrasena_hash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return nombre_completo;
    }
}
