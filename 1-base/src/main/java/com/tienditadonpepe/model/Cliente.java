package com.tienditadonpepe.model;

import java.time.LocalDate;

/** Persona que compra en la tienda. El registro es opcional: las ventas pueden ir a "Cliente general". */
public class Cliente {

    private int id_cliente;
    private String nombre_completo;
    private String dni;
    private String ruc;
    private String telefono;
    private String direccion;
    private LocalDate fecha_registro = LocalDate.now();

    // Datos calculados para la lista (no se guardan en la tabla)
    private int cantidad_compras;
    private LocalDate ultima_compra;

    public Cliente() {
    }

    public Cliente(int id_cliente, String nombre_completo, String telefono, String direccion, LocalDate fecha_registro) {
        this.id_cliente = id_cliente;
        this.nombre_completo = nombre_completo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fecha_registro = fecha_registro;
    }

    public int getId_cliente() { return id_cliente; }
    public void setId_cliente(int id_cliente) { this.id_cliente = id_cliente; }
    public String getNombre_completo() { return nombre_completo; }
    public void setNombre_completo(String nombre_completo) { this.nombre_completo = nombre_completo; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public LocalDate getFecha_registro() { return fecha_registro; }
    public void setFecha_registro(LocalDate fecha_registro) { this.fecha_registro = fecha_registro; }
    public int getCantidad_compras() { return cantidad_compras; }
    public void setCantidad_compras(int cantidad_compras) { this.cantidad_compras = cantidad_compras; }
    public LocalDate getUltima_compra() { return ultima_compra; }
    public void setUltima_compra(LocalDate ultima_compra) { this.ultima_compra = ultima_compra; }

    @Override
    public String toString() {
        return nombre_completo;
    }
}
