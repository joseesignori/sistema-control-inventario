package com.tienditadonpepe.model;

/** Empresa que abastece atún enlatado a la tienda. El código tiene el formato P001, P002, ... */
public class Proveedor {

    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_INACTIVO = "Inactivo";

    private String id_proveedor;
    private String razon_social;
    private String ruc;
    private String telefono;
    private String correo;
    private String direccion;
    private String estado = ESTADO_ACTIVO;

    public Proveedor() {
    }

    public boolean estaActivo() {
        return ESTADO_ACTIVO.equals(estado);
    }

    public String getId_proveedor() { return id_proveedor; }
    public void setId_proveedor(String id_proveedor) { this.id_proveedor = id_proveedor; }
    public String getRazon_social() { return razon_social; }
    public void setRazon_social(String razon_social) { this.razon_social = razon_social; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return razon_social;
    }
}
