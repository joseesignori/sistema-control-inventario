package com.tienditadonpepe.model;

import com.tienditadonpepe.config.AppConfig;
import com.tienditadonpepe.util.Dinero;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Venta de latas de atún con su comprobante (Figura 32, Tabla 25). El id_venta es el número de comprobante. */
public class Venta {

    public static final String TIPO_SIMPLE = "Simple";
    public static final String TIPO_FACTURA = "Factura completa";
    public static final String PAGO_EFECTIVO = "Efectivo";
    public static final String PAGO_YAPE_PLIN = "Yape-Plin";
    public static final String PAGO_TARJETA = "Tarjeta";
    public static final String ESTADO_BORRADOR = "Borrador";
    public static final String ESTADO_EMITIDA = "Emitida";
    public static final String ESTADO_ANULADA = "Anulada";

    private int id_venta;
    private LocalDateTime fecha_hora;
    private Integer id_cliente;          // null = Cliente general
    private int id_usuario;
    private String tipo_comprobante = TIPO_SIMPLE;
    private String forma_pago = PAGO_EFECTIVO;
    private String dni_ruc;
    private String direccion_entrega;
    private double subtotal;
    private double igv;
    private double total;
    private String estado = ESTADO_BORRADOR;

    private List<DetalleVenta> detalles = new ArrayList<>();

    // Datos de apoyo para listas y comprobante
    private String cliente_nombre;
    private String usuario_nombre;

    public Venta() {
    }

    /**
     * Calcula subtotal, IGV y total a partir de los detalles. Los precios de venta ya incluyen IGV:
     * en el comprobante simple total = subtotal e igv = 0; en la factura completa se discrimina el
     * IGV (18 %) contenido en el total: subtotal = total / 1.18 (base imponible).
     */
    public double calcularTotal() {
        double suma = 0;
        for (DetalleVenta d : detalles) {
            suma += d.calcularSubtotal();
        }
        total = Dinero.redondear(suma);
        if (TIPO_FACTURA.equals(tipo_comprobante)) {
            subtotal = Dinero.redondear(total / (1 + AppConfig.igv()));
            igv = Dinero.redondear(total - subtotal);
        } else {
            subtotal = total;
            igv = 0;
        }
        return total;
    }

    public boolean esFactura() {
        return TIPO_FACTURA.equals(tipo_comprobante);
    }

    public boolean estaAnulada() {
        return ESTADO_ANULADA.equals(estado);
    }

    public int unidades() {
        return detalles.stream().mapToInt(DetalleVenta::getCantidad).sum();
    }

    public int getId_venta() { return id_venta; }
    public void setId_venta(int id_venta) { this.id_venta = id_venta; }
    public LocalDateTime getFecha_hora() { return fecha_hora; }
    public void setFecha_hora(LocalDateTime fecha_hora) { this.fecha_hora = fecha_hora; }
    public Integer getId_cliente() { return id_cliente; }
    public void setId_cliente(Integer id_cliente) { this.id_cliente = id_cliente; }
    public int getId_usuario() { return id_usuario; }
    public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }
    public String getTipo_comprobante() { return tipo_comprobante; }
    public void setTipo_comprobante(String tipo_comprobante) { this.tipo_comprobante = tipo_comprobante; }
    public String getForma_pago() { return forma_pago; }
    public void setForma_pago(String forma_pago) { this.forma_pago = forma_pago; }
    public String getDni_ruc() { return dni_ruc; }
    public void setDni_ruc(String dni_ruc) { this.dni_ruc = dni_ruc; }
    public String getDireccion_entrega() { return direccion_entrega; }
    public void setDireccion_entrega(String direccion_entrega) { this.direccion_entrega = direccion_entrega; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getIgv() { return igv; }
    public void setIgv(double igv) { this.igv = igv; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
    public String getCliente_nombre() { return cliente_nombre; }
    public void setCliente_nombre(String cliente_nombre) { this.cliente_nombre = cliente_nombre; }
    public String getUsuario_nombre() { return usuario_nombre; }
    public void setUsuario_nombre(String usuario_nombre) { this.usuario_nombre = usuario_nombre; }
}
