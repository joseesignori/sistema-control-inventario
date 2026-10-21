package com.tienditadonpepe.ui;

import com.tienditadonpepe.model.MovimientoInventario;
import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Venta;
import com.tienditadonpepe.util.Fechas;
import javafx.scene.control.Label;

import java.time.LocalDate;

/** Etiquetas de estado con color (insignias) que se repiten en varias pantallas. */
public final class Insignias {

    private Insignias() {
    }

    public static Label crear(String texto, String tipo) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("badge", "badge-" + tipo);
        return l;
    }

    public static Label situacion(Producto p) {
        switch (p.situacionStock()) {
            case AGOTADO:
                return crear("Agotado", "danger");
            case CRITICO:
                return crear("Stock crítico", "danger");
            default:
                // Amarillo: aún no es crítico, pero se acerca al mínimo (aviso preventivo)
                return p.esStockBajo() ? crear("Stock bajo", "warn") : crear("Disponible", "ok");
        }
    }

    /** Fecha de vencimiento con el color del riesgo (vencido / próximo / vigente). */
    public static Label vencimiento(Producto p) {
        LocalDate f = p.getFecha_vencimiento();
        if (f == null) return crear("—", "muted");
        long dias = p.diasParaVencer();
        String texto = Fechas.fecha(f);
        if (p.estaVencido()) return crear(texto + " · vencido", "danger");
        if (p.verificarVencimiento()) return crear(texto + " · " + dias + " d", "warn");
        return crear(texto, "muted");
    }

    public static Label estadoVenta(String estado) {
        switch (estado) {
            case Venta.ESTADO_ANULADA:
                return crear("Anulada", "danger");
            case Venta.ESTADO_EMITIDA:
                return crear("Emitida", "ok");
            default:
                return crear(estado, "muted");
        }
    }

    public static Label movimiento(String tipo) {
        return MovimientoInventario.TIPO_INGRESO.equals(tipo) ? crear("↑ Ingreso", "ok") : crear("↓ Salida", "danger");
    }

    public static Label activo(String estado) {
        return "Activo".equals(estado) ? crear("Activo", "ok") : crear("Inactivo", "muted");
    }
}
