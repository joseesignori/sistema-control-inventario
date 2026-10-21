package com.tienditadonpepe.model;

import java.time.LocalDateTime;

/** Entrada o salida de latas de atún; conserva el historial de las existencias (Tabla 27). */
public class MovimientoInventario {

    public static final String TIPO_INGRESO = "Ingreso";
    public static final String TIPO_SALIDA = "Salida";

    private int id_movimiento;
    private LocalDateTime fecha_hora;
    private String tipo_movimiento;
    private int id_producto;
    private int cantidad;
    private int saldo;
    private String detalle;
    private int id_usuario;
    private Integer id_venta;        // solo si la salida/anulación proviene de una venta
    private String id_proveedor;     // solo si es un ingreso por compra

    // Datos de apoyo para mostrar en el historial
    private String producto_nombre;
    private String usuario_nombre;
    private String proveedor_nombre;

    public MovimientoInventario() {
    }

    public boolean esIngreso() {
        return TIPO_INGRESO.equals(tipo_movimiento);
    }

    /** Cantidad con signo: positiva en ingresos, negativa en salidas. */
    public int cantidadConSigno() {
        return esIngreso() ? cantidad : -cantidad;
    }

    public int getId_movimiento() { return id_movimiento; }
    public void setId_movimiento(int id_movimiento) { this.id_movimiento = id_movimiento; }
    public LocalDateTime getFecha_hora() { return fecha_hora; }
    public void setFecha_hora(LocalDateTime fecha_hora) { this.fecha_hora = fecha_hora; }
    public String getTipo_movimiento() { return tipo_movimiento; }
    public void setTipo_movimiento(String tipo_movimiento) { this.tipo_movimiento = tipo_movimiento; }
    public int getId_producto() { return id_producto; }
    public void setId_producto(int id_producto) { this.id_producto = id_producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) { this.saldo = saldo; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public int getId_usuario() { return id_usuario; }
    public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }
    public Integer getId_venta() { return id_venta; }
    public void setId_venta(Integer id_venta) { this.id_venta = id_venta; }
    public String getId_proveedor() { return id_proveedor; }
    public void setId_proveedor(String id_proveedor) { this.id_proveedor = id_proveedor; }
    public String getProducto_nombre() { return producto_nombre; }
    public void setProducto_nombre(String producto_nombre) { this.producto_nombre = producto_nombre; }
    public String getUsuario_nombre() { return usuario_nombre; }
    public void setUsuario_nombre(String usuario_nombre) { this.usuario_nombre = usuario_nombre; }
    public String getProveedor_nombre() { return proveedor_nombre; }
    public void setProveedor_nombre(String proveedor_nombre) { this.proveedor_nombre = proveedor_nombre; }
}
