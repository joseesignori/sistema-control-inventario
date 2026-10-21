package com.tienditadonpepe.controller;

import com.tienditadonpepe.config.AppConfig;
import com.tienditadonpepe.model.MovimientoInventario;
import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Reportes.VentaDia;
import com.tienditadonpepe.service.DashboardService;
import com.tienditadonpepe.ui.Icons;
import com.tienditadonpepe.ui.Insignias;
import com.tienditadonpepe.ui.Tareas;
import com.tienditadonpepe.util.Dinero;
import com.tienditadonpepe.util.Fechas;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Panel principal: indicadores del día, ventas recientes, estado del stock y alertas (RF05, RF07, RF08). */
public class DashboardController {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    @FXML private HBox kpis;
    @FXML private BarChart<String, Number> grafico;
    @FXML private CategoryAxis ejeX;
    @FXML private NumberAxis ejeY;
    @FXML private Label lblResumenGrafico;
    @FXML private VBox estado;
    @FXML private VBox alertas;
    @FXML private VBox movimientos;

    @FXML
    private void initialize() {
        ejeX.setTickLabelRotation(0);
        ejeY.setTickLabelFormatter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Number n) {
                return "S/ " + Math.round(n.doubleValue());
            }

            @Override
            public Number fromString(String s) {
                return 0;
            }
        });
        Tareas.cargar(() -> new DashboardService().cargar(), this::mostrar);
    }

    private void mostrar(DashboardService.Panel p) {
        // KPIs
        kpis.getChildren().setAll(
                kpi("STOCK TOTAL", p.stockTotal + " latas", "en " + p.productosActivos + (p.productosActivos == 1 ? " producto activo" : " productos activos"), "caja", "azul"),
                kpi("STOCK CRÍTICO", (p.criticos + p.agotados) + (p.criticos + p.agotados == 1 ? " producto" : " productos"),
                        p.stockCritico.isEmpty() ? "Todo por encima del mínimo" : "Reponer: " + resumirNombres(p.stockCritico), "alerta",
                        p.stockCritico.isEmpty() ? "verde" : "rojo"),
                kpi("PRÓXIMOS A VENCER", p.porVencer.size() + (p.porVencer.size() == 1 ? " producto" : " productos"),
                        "Vencen en los próximos " + AppConfig.diasAlertaVencimiento() + " días", "calendario", p.porVencer.isEmpty() ? "verde" : "ambar"),
                kpi("VENTAS DEL MES", Dinero.soles(p.ventasMes.total()), p.ventasMes.ventas() + " comprobantes · hoy " + Dinero.soles(p.ventasHoy.total()),
                        "dinero", "verde"));

        // Gráfico de barras
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        double total = 0;
        int dias = 0;
        LocalDate hoy = LocalDate.now();
        for (VentaDia d : p.ventasPorDia) {
            XYChart.Data<String, Number> dato = new XYChart.Data<>(DIA.format(d.fecha()), d.total());
            serie.getData().add(dato);
            total += d.total();
            if (d.total() > 0) dias++;
            final boolean esHoy = d.fecha().equals(hoy);
            dato.nodeProperty().addListener((o, a, nodo) -> {
                if (nodo == null) return;
                if (esHoy) nodo.getStyleClass().add("hoy");
                Tooltip.install(nodo, new Tooltip(Fechas.fecha(d.fecha()) + "\n" + Dinero.soles(d.total()) + " · " + d.ventas() + " comprobantes · "
                        + d.unidades() + " latas"));
            });
        }
        grafico.getData().setAll(serie);
        lblResumenGrafico.setText("Total " + Dinero.soles(total) + " · promedio diario " + Dinero.soles(dias == 0 ? 0 : total / dias) + " en días con venta");

        // Estado del inventario
        estado.getChildren().clear();
        for (Producto pr : p.productos) estado.getChildren().add(filaEstado(pr));

        // Alertas
        alertas.getChildren().clear();
        if (p.stockCritico.isEmpty() && p.porVencer.isEmpty()) {
            alertas.getChildren().add(filaAlerta("ok", "check", "Sin alertas", "Todos los productos tienen stock suficiente y vigencia segura."));
        }
        for (Producto pr : p.stockCritico) {
            alertas.getChildren().add(filaAlerta("danger", "alerta",
                    (pr.getStock_actual() == 0 ? "Agotado · " : "Stock crítico · ") + pr.nombreCompleto() + " " + pr.getMarca(),
                    pr.getStock_actual() + " latas disponibles · mínimo configurado " + pr.getStock_minimo() + " · proveedor " + pr.getProveedor_nombre()));
        }
        for (Producto pr : p.porVencer) {
            String det = pr.estaVencido() ? "Vencido el " + Fechas.fecha(pr.getFecha_vencimiento())
                    : "Vence el " + Fechas.fecha(pr.getFecha_vencimiento()) + " (en " + pr.diasParaVencer() + " días)";
            alertas.getChildren().add(filaAlerta("warn", "calendario", "Próximo a vencer · " + pr.nombreCompleto() + " " + pr.getMarca(),
                    det + " · " + pr.getStock_actual() + " latas en stock"));
        }

        // Últimos movimientos
        movimientos.getChildren().clear();
        if (p.ultimosMovimientos.isEmpty()) {
            Label l = new Label("Aún no hay movimientos registrados.");
            l.getStyleClass().add("texto-muted");
            movimientos.getChildren().add(l);
        }
        for (MovimientoInventario m : p.ultimosMovimientos) movimientos.getChildren().add(filaMovimiento(m));
    }

    private static String resumirNombres(java.util.List<Producto> lista) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(", ");
            Producto p = lista.get(i);
            sb.append(p.getNombre().replace("Atún ", "")).append(' ').append(p.getPresentacion().replace("Lata ", ""));
        }
        return sb.toString();
    }

    private VBox kpi(String titulo, String valor, String detalle, String icono, String color) {
        Label t = new Label(titulo);
        t.getStyleClass().add("kpi-titulo");
        Label v = new Label(valor);
        v.getStyleClass().add("kpi-valor");
        Label d = new Label(detalle);
        d.getStyleClass().add("kpi-detalle");
        d.setWrapText(true);
        StackPane ic = new StackPane(Icons.crear(icono, 20));
        ic.getStyleClass().addAll("kpi-icono", "kpi-" + color);
        HBox cab = new HBox(t);
        HBox.setHgrow(t, Priority.ALWAYS);
        t.setMaxWidth(Double.MAX_VALUE);
        cab.getChildren().add(ic);
        cab.setAlignment(Pos.TOP_LEFT);
        VBox caja = new VBox(2, cab, v, d);
        caja.getStyleClass().addAll("card", "kpi");
        HBox.setHgrow(caja, Priority.ALWAYS);
        caja.setMaxWidth(Double.MAX_VALUE);
        caja.setPrefWidth(200);
        caja.setMinHeight(128);
        return caja;
    }

    private Node filaEstado(Producto p) {
        String clase = p.situacionStock() != Producto.SituacionStock.DISPONIBLE ? "danger" : p.esStockBajo() ? "warn" : "ok";
        Label nombre = new Label(p.getNombre() + " · " + p.getPresentacion());
        nombre.getStyleClass().add("negrita");
        Label marca = new Label(p.getMarca());
        marca.getStyleClass().addAll("texto-muted", "texto-peq");
        VBox izq = new VBox(1, nombre, marca);
        Label cantidad = new Label(p.getStock_actual() + " latas");
        cantidad.getStyleClass().add("negrita");
        Label minimo = new Label("mín. " + p.getStock_minimo());
        minimo.getStyleClass().addAll("texto-muted", "texto-peq");
        VBox der = new VBox(1, cantidad, minimo);
        der.setAlignment(Pos.CENTER_RIGHT);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox cab = new HBox(8, izq, espacio, der);
        cab.setAlignment(Pos.CENTER_LEFT);

        double tope = Math.max(p.getStock_actual(), p.getStock_minimo() * 3.0);
        if (tope <= 0) tope = 1;
        Pane pista = new Pane();
        pista.getStyleClass().add("barra-fondo");
        pista.setPrefHeight(8);
        Region relleno = new Region();
        relleno.getStyleClass().add("barra-relleno-" + clase);
        relleno.setPrefHeight(8);
        relleno.setMinHeight(8);
        relleno.prefWidthProperty().bind(pista.widthProperty().multiply(p.getStock_actual() / tope));
        Region marcaMin = new Region();
        marcaMin.setStyle("-fx-background-color: #1A2A39;");
        marcaMin.setPrefSize(2, 14);
        marcaMin.setLayoutY(-3);
        marcaMin.layoutXProperty().bind(pista.widthProperty().multiply(p.getStock_minimo() / tope).subtract(1));
        pista.getChildren().addAll(relleno, marcaMin);
        HBox badges = new HBox(8, Insignias.situacion(p), Insignias.vencimiento(p));
        badges.setPadding(new Insets(2, 0, 0, 0));
        VBox caja = new VBox(6, cab, pista, badges);
        return caja;
    }

    private Node filaAlerta(String tipo, String icono, String titulo, String detalle) {
        Label t = new Label(titulo);
        t.getStyleClass().add("alerta-titulo");
        t.setWrapText(true);
        Label d = new Label(detalle);
        d.getStyleClass().add("alerta-detalle");
        d.setWrapText(true);
        VBox textos = new VBox(2, t, d);
        HBox.setHgrow(textos, Priority.ALWAYS);
        HBox fila = new HBox(12, Icons.crear(icono, 20, "icono-" + tipo), textos);
        fila.getStyleClass().addAll("alerta-fila", "alerta-" + tipo);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private Node filaMovimiento(MovimientoInventario m) {
        boolean ingreso = m.esIngreso();
        StackPane ic = new StackPane(Icons.crear(ingreso ? "sube" : "baja", 16, ingreso ? "icono-ok" : "icono-danger"));
        ic.getStyleClass().addAll("kpi-icono", ingreso ? "kpi-verde" : "kpi-rojo");
        ic.setMinSize(32, 32);
        ic.setPrefSize(32, 32);
        Label t = new Label(m.getProducto_nombre());
        t.getStyleClass().add("negrita");
        t.setMaxWidth(300);
        Label d = new Label(m.getDetalle() == null ? m.getTipo_movimiento() : m.getDetalle());
        d.getStyleClass().addAll("texto-muted", "texto-peq");
        d.setMaxWidth(300);
        VBox textos = new VBox(1, t, d);
        HBox.setHgrow(textos, Priority.ALWAYS);
        Label cant = new Label((ingreso ? "+" : "−") + m.getCantidad());
        cant.getStyleClass().addAll("negrita", ingreso ? "texto-ok" : "texto-danger");
        Label hora = new Label(Fechas.fechaHora(m.getFecha_hora()));
        hora.getStyleClass().addAll("texto-muted", "texto-peq");
        VBox der = new VBox(1, cant, hora);
        der.setAlignment(Pos.CENTER_RIGHT);
        HBox fila = new HBox(12, ic, textos, der);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setStyle("-fx-padding: 8 0 8 0; -fx-border-color: transparent transparent #EEF2F6 transparent;");
        return fila;
    }

    @FXML
    private void irProductos() {
        MainController.ir("productos");
    }

    @FXML
    private void irMovimientos() {
        MainController.ir("movimientos");
    }
}
