package com.tienditadonpepe.model;

import com.tienditadonpepe.util.Dinero;

/** Línea de una venta: producto, cantidad y precio al momento de la venta (Tabla 26). */
public class DetalleVenta {

    private int id_detalle;
    private int id_venta;
    private int id_producto;
    private int cantidad;
    private double precio_unitario;
    private double subtotal;

    /** Producto asociado (se completa al consultar o al armar el carrito). */
    private Producto producto;

    public DetalleVenta() {
    }

    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.id_producto = producto.getId_producto();
        this.cantidad = cantidad;
        this.precio_unitario = producto.getPrecio_venta();
        calcularSubtotal();
    }

    /** subtotal = cantidad × precio_unitario */
    public double calcularSubtotal() {
        subtotal = Dinero.redondear(cantidad * precio_unitario);
        return subtotal;
    }

    public int getId_detalle() { return id_detalle; }
    public void setId_detalle(int id_detalle) { this.id_detalle = id_detalle; }
    public int getId_venta() { return id_venta; }
    public void setId_venta(int id_venta) { this.id_venta = id_venta; }
    public int getId_producto() { return id_producto; }
    public void setId_producto(int id_producto) { this.id_producto = id_producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecio_unitario() { return precio_unitario; }
    public void setPrecio_unitario(double precio_unitario) { this.precio_unitario = precio_unitario; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
}
