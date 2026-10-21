package com.tienditadonpepe.model;

import com.tienditadonpepe.config.AppConfig;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Atún enlatado administrado por el sistema (Figura 32, Tabla 24). */
public class Producto {

    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_INACTIVO = "Inactivo";

    /** Situación del stock respecto al mínimo configurado (RF07). */
    public enum SituacionStock {
        DISPONIBLE("Disponible"), CRITICO("Stock crítico"), AGOTADO("Agotado");

        private final String etiqueta;

        SituacionStock(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

    private int id_producto;
    private String nombre;
    private String marca;
    private String presentacion;
    private double precio_venta;
    private int stock_actual;
    private int stock_minimo;
    private LocalDate fecha_vencimiento;
    private String estado = ESTADO_ACTIVO;
    private String id_proveedor;

    // Dato de apoyo para mostrar en tablas (no se guarda en producto)
    private String proveedor_nombre;

    public Producto() {
    }

    // ---- Operaciones del diagrama de clases (lógica propia de la entidad) ------------------

    /** RF07: el stock es crítico cuando la cantidad disponible es igual o inferior al stock mínimo. */
    public boolean verificarStockCritico() {
        return stock_actual <= stock_minimo;
    }

    /**
     * Stock «bajo» (poco crítico): todavía está por encima del mínimo, pero a menos de un 50 % de distancia.
     * Es un aviso preventivo (amarillo); el stock crítico propiamente dicho (rojo) empieza en el mínimo.
     */
    public boolean esStockBajo() {
        return !verificarStockCritico() && stock_actual <= Math.ceil(stock_minimo * 1.5);
    }

    /** RF08: true si el lote ya venció o vence dentro del plazo de alerta configurado. */
    public boolean verificarVencimiento() {
        return fecha_vencimiento != null
                && !fecha_vencimiento.isAfter(LocalDate.now().plusDays(AppConfig.diasAlertaVencimiento()));
    }

    // ---- Apoyo ----------------------------------------------------------------------------

    public boolean estaVencido() {
        return fecha_vencimiento != null && fecha_vencimiento.isBefore(LocalDate.now());
    }

    public long diasParaVencer() {
        return fecha_vencimiento == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), fecha_vencimiento);
    }

    public SituacionStock situacionStock() {
        if (stock_actual <= 0) return SituacionStock.AGOTADO;
        return verificarStockCritico() ? SituacionStock.CRITICO : SituacionStock.DISPONIBLE;
    }

    public boolean estaActivo() {
        return ESTADO_ACTIVO.equals(estado);
    }

    /** Ejemplo: "Atún en aceite · Lata 170 g" */
    public String nombreCompleto() {
        return nombre + " · " + presentacion;
    }

    public int getId_producto() { return id_producto; }
    public void setId_producto(int id_producto) { this.id_producto = id_producto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public double getPrecio_venta() { return precio_venta; }
    public void setPrecio_venta(double precio_venta) { this.precio_venta = precio_venta; }
    public int getStock_actual() { return stock_actual; }
    public void setStock_actual(int stock_actual) { this.stock_actual = stock_actual; }
    public int getStock_minimo() { return stock_minimo; }
    public void setStock_minimo(int stock_minimo) { this.stock_minimo = stock_minimo; }
    public LocalDate getFecha_vencimiento() { return fecha_vencimiento; }
    public void setFecha_vencimiento(LocalDate fecha_vencimiento) { this.fecha_vencimiento = fecha_vencimiento; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getId_proveedor() { return id_proveedor; }
    public void setId_proveedor(String id_proveedor) { this.id_proveedor = id_proveedor; }
    public String getProveedor_nombre() { return proveedor_nombre; }
    public void setProveedor_nombre(String proveedor_nombre) { this.proveedor_nombre = proveedor_nombre; }

    @Override
    public String toString() {
        return nombreCompleto();
    }
}
