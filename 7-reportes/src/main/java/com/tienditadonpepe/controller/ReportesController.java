package com.tienditadonpepe.controller;

import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Reportes.VentaDia;
import com.tienditadonpepe.model.Reportes.VentaPago;
import com.tienditadonpepe.model.Reportes.VentaProducto;
import com.tienditadonpepe.service.Exportador;
import com.tienditadonpepe.service.ProductoService;
import com.tienditadonpepe.service.ReporteService;
import com.tienditadonpepe.service.ReporteService.FilaInventario;
import com.tienditadonpepe.service.ReporteService.Indicadores;
import com.tienditadonpepe.service.ReporteService.ReporteVentas;
import com.tienditadonpepe.ui.Dialogs;
import com.tienditadonpepe.ui.Formulario;
import com.tienditadonpepe.ui.Icons;
import com.tienditadonpepe.ui.Insignias;
import com.tienditadonpepe.ui.Tablas;
import com.tienditadonpepe.ui.Tareas;
import com.tienditadonpepe.ui.Toast;
import com.tienditadonpepe.config.AppConfig;
import com.tienditadonpepe.util.Dinero;
import com.tienditadonpepe.util.Fechas;
import com.tienditadonpepe.util.NegocioException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** RF10 – Reportes de ventas e inventario e indicadores mensuales (apartado 1.2.2 del informe). */
public class ReportesController {

    @FXML private VBox tabVentas;
    @FXML private VBox tabInventario;
    @FXML private VBox tabIndicadores;

    private final ReporteService servicio = new ReporteService();

    // Ventas
    private DatePicker dpDesde;
    private DatePicker dpHasta;
    private VBox resultadoVentas;
    private ReporteVentas ultimoVentas;
    private Button btnVentasPdf;
    private Button btnVentasCsv;

    // Inventario
    private TableView<FilaInventario> tablaInventario;
    private Label lblTotalInventario;
    private List<FilaInventario> datosInventario = new ArrayList<>();

    // Indicadores
    private ComboBox<YearMonth> cmbMes;
    private VBox panelConteo;
    private final Map<Integer, TextField> camposConteo = new LinkedHashMap<>();
    private FlowPane tarjetasIndicadores;
    private Indicadores ultimosIndicadores;
    private Button btnIndPdf;

    @FXML
    private void initialize() {
        construirVentas();
        construirInventario();
        construirIndicadores();
        generarVentas();
        cargarInventario();
        calcularIndicadores();
    }

    // =====================================================================================
    // Ventas
    // =====================================================================================

    private void construirVentas() {
        dpDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
        dpHasta = new DatePicker(LocalDate.now());
        Formulario.formatear(dpDesde);
        Formulario.formatear(dpHasta);
        dpDesde.setPrefWidth(140);
        dpHasta.setPrefWidth(140);
        ComboBox<String> rapido = new ComboBox<>();
        rapido.getItems().addAll("Personalizado", "Hoy", "Últimos 7 días", "Este mes", "Mes anterior");
        rapido.getSelectionModel().select("Este mes");
        rapido.setPrefWidth(160);
        rapido.valueProperty().addListener((o, a, b) -> {
            LocalDate hoy = LocalDate.now();
            if ("Hoy".equals(b)) {
                dpDesde.setValue(hoy);
                dpHasta.setValue(hoy);
            } else if ("Últimos 7 días".equals(b)) {
                dpDesde.setValue(hoy.minusDays(6));
                dpHasta.setValue(hoy);
            } else if ("Este mes".equals(b)) {
                dpDesde.setValue(hoy.withDayOfMonth(1));
                dpHasta.setValue(hoy);
            } else if ("Mes anterior".equals(b)) {
                LocalDate ini = hoy.minusMonths(1).withDayOfMonth(1);
                dpDesde.setValue(ini);
                dpHasta.setValue(ini.withDayOfMonth(ini.lengthOfMonth()));
            }
            if (!"Personalizado".equals(b)) generarVentas();
        });
        Button generar = new Button("Generar reporte", Icons.crear("grafico", 16, "icono-blanco"));
        generar.getStyleClass().add("btn-primary");
        generar.setOnAction(e -> {
            rapido.getSelectionModel().select("Personalizado");
            generarVentas();
        });
        btnVentasPdf = new Button("PDF", Icons.crear("documento", 16));
        btnVentasPdf.setOnAction(e -> exportarVentas(true));
        btnVentasCsv = new Button("CSV", Icons.crear("descargar", 16));
        btnVentasCsv.setOnAction(e -> exportarVentas(false));
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        HBox barra = new HBox(12, campo("PERIODO", rapido), campo("DESDE", dpDesde), campo("HASTA", dpHasta), campo(" ", generar), esp,
                campo(" ", new HBox(8, btnVentasPdf, btnVentasCsv)));
        barra.getStyleClass().add("barra-filtros");
        barra.setAlignment(Pos.BOTTOM_LEFT);
        resultadoVentas = new VBox(16);
        ScrollPane sp = new ScrollPane(resultadoVentas);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(sp, Priority.ALWAYS);
        tabVentas.getChildren().addAll(barra, sp);
    }

    private VBox campo(String etiqueta, Node nodo) {
        Label l = new Label(etiqueta);
        l.getStyleClass().add("filtro-etiqueta");
        return new VBox(4, l, nodo);
    }

    private void generarVentas() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        Tareas.ejecutar(() -> servicio.reporteVentas(desde, hasta), this::mostrarVentas, t -> {
            if (t instanceof NegocioException) Toast.error(t.getMessage());
            else Tareas.mostrarError(t);
        });
    }

    private void mostrarVentas(ReporteVentas r) {
        ultimoVentas = r;
        resultadoVentas.getChildren().clear();
        HBox kpis = new HBox(14,
                miniKpi("COMPROBANTES EMITIDOS", String.valueOf(r.resumen.ventas()), r.anuladas + " anulados en el período", "documento", "azul"),
                miniKpi("LATAS VENDIDAS", String.valueOf(r.resumen.unidades()), "unidades de atún enlatado", "caja", "verde"),
                miniKpi("TOTAL VENDIDO", Dinero.soles(r.resumen.total()), "incluye IGV", "dinero", "verde"),
                miniKpi("TICKET PROMEDIO", Dinero.soles(r.resumen.ticketPromedio()), "por comprobante", "tendencia", "ambar"));
        resultadoVentas.getChildren().add(kpis);

        // Gráfico por día
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return "S/ " + Math.round(n.doubleValue());
            }

            @Override
            public Number fromString(String s) {
                return 0;
            }
        });
        BarChart<String, Number> grafico = new BarChart<>(x, y);
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setCategoryGap(8);
        grafico.setMinHeight(240);
        grafico.setPrefHeight(260);
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM");
        for (VentaDia d : r.porDia) {
            XYChart.Data<String, Number> dato = new XYChart.Data<>(f.format(d.fecha()), d.total());
            serie.getData().add(dato);
            dato.nodeProperty().addListener((o, a, n) -> {
                if (n != null) Tooltip.install(n, new Tooltip(Fechas.fecha(d.fecha()) + "\n" + Dinero.soles(d.total()) + " · " + d.ventas() + " comprobantes"));
            });
        }
        grafico.getData().add(serie);
        VBox cardGrafico = card("Ventas por día", Fechas.fecha(r.desde) + " – " + Fechas.fecha(r.hasta), grafico);
        HBox.setHgrow(cardGrafico, Priority.ALWAYS);
        cardGrafico.setPrefWidth(560);

        TableView<VentaProducto> tp = new TableView<>();
        Tablas.configurar(tp, "Sin ventas en el período.", "caja");
        tp.getColumns().add(Tablas.texto("PRODUCTO", 200, VentaProducto::producto));
        tp.getColumns().add(Tablas.textoDerecha("LATAS", 60, v -> String.valueOf(v.unidades())));
        tp.getColumns().add(Tablas.textoDerecha("IMPORTE", 90, v -> Dinero.soles(v.total())));
        tp.setItems(FXCollections.observableArrayList(r.porProducto));
        ajustarAlto(tp, r.porProducto.size());

        TableView<VentaPago> tf = new TableView<>();
        Tablas.configurar(tf, "Sin ventas en el período.", "dinero");
        tf.getColumns().add(Tablas.texto("FORMA DE PAGO", 160, VentaPago::formaPago));
        tf.getColumns().add(Tablas.textoDerecha("COMPROB.", 70, v -> String.valueOf(v.ventas())));
        tf.getColumns().add(Tablas.textoDerecha("IMPORTE", 90, v -> Dinero.soles(v.total())));
        tf.setItems(FXCollections.observableArrayList(r.porFormaPago));
        ajustarAlto(tf, r.porFormaPago.size());

        VBox derecha = new VBox(14, card("Ventas por producto", null, tp), card("Ventas por forma de pago", null, tf));
        derecha.setPrefWidth(420);
        derecha.setMinWidth(380);
        resultadoVentas.getChildren().add(new HBox(14, cardGrafico, derecha));

        TableView<com.tienditadonpepe.model.Reportes.VentaUsuario> tu = new TableView<>();
        Tablas.configurar(tu, "Sin ventas en el período.", "usuarios");
        tu.getColumns().add(Tablas.texto("CAJERO", 240, com.tienditadonpepe.model.Reportes.VentaUsuario::usuario));
        tu.getColumns().add(Tablas.textoDerecha("COMPROBANTES", 100, v -> String.valueOf(v.ventas())));
        tu.getColumns().add(Tablas.textoDerecha("IMPORTE", 100, v -> Dinero.soles(v.total())));
        tu.setItems(FXCollections.observableArrayList(r.porUsuario));
        ajustarAlto(tu, r.porUsuario.size());
        resultadoVentas.getChildren().add(card("Ventas por usuario", null, tu));
    }

    private static void ajustarAlto(TableView<?> t, int filas) {
        double alto = 46 + Math.max(1, filas) * 46 + 8;
        t.setPrefHeight(alto);
        t.setMinHeight(alto);
        t.setMaxHeight(alto);
    }

    private VBox card(String titulo, String sub, Node contenido) {
        Label t = new Label(titulo);
        t.getStyleClass().add("card-titulo");
        VBox cab = new VBox(2, t);
        if (sub != null) {
            Label s = new Label(sub);
            s.setWrapText(true);
            s.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            s.getStyleClass().add("card-sub");
            cab.getChildren().add(s);
        }
        VBox c = new VBox(10, cab, contenido);
        c.getStyleClass().addAll("card", "card-pad");
        return c;
    }

    private VBox miniKpi(String titulo, String valor, String detalle, String icono, String color) {
        Label t = new Label(titulo);
        t.getStyleClass().add("kpi-titulo");
        Label v = new Label(valor);
        v.getStyleClass().add("kpi-valor");
        Label d = new Label(detalle);
        d.getStyleClass().add("kpi-detalle");
        VBox c = new VBox(2, t, v, d);
        c.getStyleClass().addAll("card", "kpi");
        HBox.setHgrow(c, Priority.ALWAYS);
        c.setMaxWidth(Double.MAX_VALUE);
        c.setPrefWidth(200);
        return c;
    }

    private void exportarVentas(boolean pdf) {
        if (ultimoVentas == null) return;
        String nombre = "reporte_ventas_" + ultimoVentas.desde + "_" + ultimoVentas.hasta + (pdf ? ".pdf" : ".csv");
        File f = MovimientosController.elegirArchivo("Exportar reporte de ventas", nombre, pdf ? "Documento PDF" : "Archivo CSV", pdf ? "*.pdf" : "*.csv");
        if (f == null) return;
        try {
            if (pdf) Exportador.ventasPdf(ultimoVentas, f.toPath());
            else Exportador.ventasCsv(ultimoVentas, f.toPath());
            Toast.ok("Reporte guardado: " + f.getName());
        } catch (IOException e) {
            Dialogs.error("No se pudo exportar", e.getMessage());
        }
    }

    // =====================================================================================
    // Inventario valorizado
    // =====================================================================================

    private void construirInventario() {
        Button pdf = new Button("PDF", Icons.crear("documento", 16));
        pdf.setOnAction(e -> exportarInventario(true));
        Button csv = new Button("CSV", Icons.crear("descargar", 16));
        csv.setOnAction(e -> exportarInventario(false));
        Label info = new Label("Existencias actuales valorizadas a precio de venta.");
        info.getStyleClass().add("texto-muted");
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        HBox barra = new HBox(10, info, esp, pdf, csv);
        barra.getStyleClass().add("barra-filtros");
        tablaInventario = new TableView<>();
        Tablas.configurar(tablaInventario, "No hay productos registrados.", "caja");
        tablaInventario.getColumns().add(Tablas.nodo("PRODUCTO", 240, f -> {
            Label n = new Label(f.producto.getNombre() + " · " + f.producto.getPresentacion());
            n.getStyleClass().add("negrita");
            Label m = new Label(f.producto.getMarca());
            m.getStyleClass().addAll("texto-muted", "texto-peq");
            return new VBox(1, n, m);
        }));
        tablaInventario.getColumns().add(Tablas.textoCentro("STOCK", 70, f -> String.valueOf(f.producto.getStock_actual())));
        tablaInventario.getColumns().add(Tablas.textoCentro("MÍNIMO", 70, f -> String.valueOf(f.producto.getStock_minimo())));
        tablaInventario.getColumns().add(Tablas.nodoCentro("SITUACIÓN", 120, f -> Insignias.situacion(f.producto)));
        tablaInventario.getColumns().add(Tablas.nodoCentro("VENCIMIENTO", 160, f -> Insignias.vencimiento(f.producto)));
        tablaInventario.getColumns().add(Tablas.textoDerecha("P. VENTA", 90, f -> Dinero.soles(f.producto.getPrecio_venta())));
        tablaInventario.getColumns().add(Tablas.textoDerecha("VALOR", 100, f -> Dinero.soles(f.valor)));
        lblTotalInventario = new Label();
        lblTotalInventario.getStyleClass().add("negrita");
        VBox.setVgrow(tablaInventario, Priority.ALWAYS);
        tabInventario.getChildren().addAll(barra, tablaInventario, lblTotalInventario);
    }

    private void cargarInventario() {
        Tareas.cargar(servicio::reporteInventario, datos -> {
            datosInventario = datos;
            tablaInventario.setItems(FXCollections.observableArrayList(datos));
            int unidades = datos.stream().mapToInt(f -> f.producto.getStock_actual()).sum();
            double valor = datos.stream().mapToDouble(f -> f.valor).sum();
            lblTotalInventario.setText(unidades + " latas en inventario · valor a precio de venta " + Dinero.soles(valor));
        });
    }

    private void exportarInventario(boolean pdf) {
        String nombre = "inventario_" + LocalDate.now() + (pdf ? ".pdf" : ".csv");
        File f = MovimientosController.elegirArchivo("Exportar inventario", nombre, pdf ? "Documento PDF" : "Archivo CSV", pdf ? "*.pdf" : "*.csv");
        if (f == null) return;
        try {
            if (pdf) Exportador.inventarioPdf(datosInventario, f.toPath());
            else Exportador.inventarioCsv(datosInventario, f.toPath());
            Toast.ok("Reporte guardado: " + f.getName());
        } catch (IOException e) {
            Dialogs.error("No se pudo exportar", e.getMessage());
        }
    }

    // =====================================================================================
    // Indicadores mensuales
    // =====================================================================================

    private void construirIndicadores() {
        cmbMes = new ComboBox<>();
        YearMonth actual = YearMonth.now();
        for (int i = 0; i < 12; i++) cmbMes.getItems().add(actual.minusMonths(i));
        cmbMes.setConverter(new StringConverter<>() {
            @Override
            public String toString(YearMonth m) {
                if (m == null) return "";
                String s = Fechas.mesAnio(m);
                return Character.toUpperCase(s.charAt(0)) + s.substring(1);
            }

            @Override
            public YearMonth fromString(String s) {
                return null;
            }
        });
        cmbMes.getSelectionModel().selectFirst();
        cmbMes.setPrefWidth(190);
        cmbMes.valueProperty().addListener((o, a, b) -> calcularIndicadores());
        Button calcular = new Button("Calcular", Icons.crear("refrescar", 16, "icono-blanco"));
        calcular.getStyleClass().add("btn-primary");
        calcular.setOnAction(e -> calcularIndicadores());
        btnIndPdf = new Button("PDF", Icons.crear("documento", 16));
        btnIndPdf.setOnAction(e -> exportarIndicadores());
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        HBox barra = new HBox(12, campo("MES A EVALUAR", cmbMes), campo(" ", calcular), esp, campo(" ", btnIndPdf));
        barra.getStyleClass().add("barra-filtros");
        barra.setAlignment(Pos.BOTTOM_LEFT);

        panelConteo = new VBox(8);
        VBox cardConteo = card("Conteo físico (opcional)", "Ingresa lo contado en estantes y almacén para calcular la exactitud del inventario.", panelConteo);
        cardConteo.setPrefWidth(380);
        cardConteo.setMinWidth(340);
        cardConteo.setMaxWidth(420);
        tarjetasIndicadores = new FlowPane(14, 14);
        tarjetasIndicadores.setPrefWrapLength(640);
        HBox.setHgrow(tarjetasIndicadores, Priority.ALWAYS);
        HBox cuerpo = new HBox(14, tarjetasIndicadores, cardConteo);
        cuerpo.setAlignment(Pos.TOP_LEFT);
        ScrollPane sp = new ScrollPane(cuerpo);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(sp, Priority.ALWAYS);
        tabIndicadores.getChildren().addAll(barra, sp);
        Tareas.cargar(() -> new ProductoService().listarActivos(), this::armarConteo);
    }

    private void armarConteo(List<Producto> lista) {
        panelConteo.getChildren().clear();
        camposConteo.clear();
        for (Producto p : lista) {
            Label n = new Label(p.getNombre() + " · " + p.getPresentacion());
            n.getStyleClass().add("negrita");
            Label m = new Label(p.getMarca());
            m.getStyleClass().addAll("texto-muted", "texto-peq");
            VBox textos = new VBox(1, n, m);
            HBox.setHgrow(textos, Priority.ALWAYS);
            TextField campo = new TextField();
            campo.setPromptText("Contado");
            campo.setPrefWidth(90);
            camposConteo.put(p.getId_producto(), campo);
            HBox fila = new HBox(10, textos, campo);
            fila.setAlignment(Pos.CENTER_LEFT);
            panelConteo.getChildren().add(fila);
        }
        Button aplicar = new Button("Calcular con este conteo", Icons.crear("check", 16, "icono-blanco"));
        aplicar.getStyleClass().add("btn-ok");
        aplicar.setMaxWidth(Double.MAX_VALUE);
        aplicar.setOnAction(e -> calcularIndicadores());
        panelConteo.getChildren().add(aplicar);
    }

    private Map<Integer, Integer> leerConteo() {
        Map<Integer, Integer> conteo = new HashMap<>();
        for (Map.Entry<Integer, TextField> e : camposConteo.entrySet()) {
            String t = e.getValue().getText();
            if (t == null || t.isBlank()) continue;
            try {
                int v = Integer.parseInt(t.trim());
                if (v < 0) throw new NumberFormatException();
                conteo.put(e.getKey(), v);
            } catch (NumberFormatException ex) {
                throw new NegocioException("El conteo físico debe ser un número entero (0 o mayor).");
            }
        }
        return conteo;
    }

    private void calcularIndicadores() {
        YearMonth mes = cmbMes.getValue();
        Map<Integer, Integer> conteo;
        try {
            conteo = leerConteo();
        } catch (NegocioException e) {
            Toast.error(e.getMessage());
            return;
        }
        Tareas.ejecutar(() -> servicio.indicadoresMensuales(mes, conteo), this::mostrarIndicadores, Tareas::mostrarError);
    }

    private static final String[] TITULOS = {"Exactitud del inventario", "Latas en stock crítico", "Porcentaje de agotamiento",
            "Latas próximas a vencer", "Tiempo de actualización"};
    private static final String[] FORMULAS = {"(Stock físico ÷ Stock registrado) × 100", "(Unidades con stock ≤ mínimo ÷ Total de unidades disponibles) × 100",
            "(Ocasiones con stock = 0 ÷ Períodos evaluados) × 100", "(Unidades próximas a vencer ÷ Total de unidades disponibles) × 100",
            "Hora de registro − Hora del movimiento"};
    private static final String[] ICONOS = {"check", "alerta", "prohibido", "calendario", "reloj"};

    private void mostrarIndicadores(Indicadores ind) {
        ultimosIndicadores = ind;
        tarjetasIndicadores.getChildren().clear();
        List<String[]> filas = Exportador.filasIndicadores(ind);
        for (int i = 0; i < filas.size(); i++) {
            String[] f = filas.get(i);
            Label titulo = new Label(TITULOS[i].toUpperCase());
            titulo.getStyleClass().add("kpi-titulo");
            Label valor = new Label(f[1]);
            valor.getStyleClass().add("indicador-valor");
            Label calculo = new Label(f[2]);
            calculo.getStyleClass().add("texto-peq");
            calculo.setWrapText(true);
            calculo.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            Label formula = new Label(FORMULAS[i]);
            formula.getStyleClass().addAll("texto-muted", "texto-peq");
            formula.setWrapText(true);
            formula.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            VBox c = new VBox(4, titulo, valor, calculo, formula);
            c.getStyleClass().addAll("card", "indicador-card");
            c.setPrefWidth(300);
            c.setMinHeight(150);
            tarjetasIndicadores.getChildren().add(c);
        }
        Label nota = new Label("Mes evaluado: " + Fechas.mesAnio(ind.mes) + " · cierre al " + Fechas.fecha(ind.fechaCierre)
                + ". El stock de cierre se reconstruye con el saldo de cada movimiento del historial.");
        nota.getStyleClass().addAll("mensaje-info");
        nota.setWrapText(true);
        nota.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        nota.setPrefWidth(620);
        tarjetasIndicadores.getChildren().add(nota);
    }

    private void exportarIndicadores() {
        if (ultimosIndicadores == null) return;
        File f = MovimientosController.elegirArchivo("Exportar indicadores", "indicadores_" + ultimosIndicadores.mes + ".pdf", "Documento PDF", "*.pdf");
        if (f == null) return;
        try {
            Exportador.indicadoresPdf(ultimosIndicadores, f.toPath());
            Toast.ok("Reporte guardado: " + f.getName());
        } catch (IOException e) {
            Dialogs.error("No se pudo exportar", e.getMessage());
        }
    }
}
