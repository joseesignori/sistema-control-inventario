package com.tienditadonpepe.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.util.function.Function;

/** Fábricas de columnas y configuración común de las tablas. */
public final class Tablas {

    private Tablas() {
    }

    public static <S> void configurar(TableView<S> tabla, String mensajeVacio, String icono) {
        // El reparto de anchos lo hace esta clase (ver repartir): así cada columna recibe lo que le corresponde
        // y las cifras, fechas e insignias nunca se recortan mientras haya espacio.
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabla.setFixedCellSize(46);
        tabla.setPlaceholder(vacio(mensajeVacio, icono));
        tabla.setFocusTraversable(false);
        InvalidationListener alRedimensionar = o -> repartir(tabla);
        tabla.widthProperty().addListener(alRedimensionar);
        tabla.heightProperty().addListener(alRedimensionar);
        tabla.getColumns().addListener(alRedimensionar);
        tabla.itemsProperty().addListener(alRedimensionar);
        tabla.itemsProperty().addListener((o, antes, ahora) -> {
            if (ahora != null) ahora.addListener(alRedimensionar);
        });
    }

    /** Reparte el ancho disponible entre las columnas en proporción a su ancho deseado, respetando el mínimo de cada una. */
    private static void repartir(TableView<?> tabla) {
        int n = tabla.getColumns().size();
        double total = tabla.getWidth();
        if (n == 0 || total <= 0) return;
        boolean conBarra = tabla.getItems() != null && tabla.getItems().size() * 46.0 + 42 > tabla.getHeight();
        double disponible = total - 2 - (conBarra ? 14 : 0);
        double[] ancho = new double[n];
        double[] minimo = new double[n];
        boolean[] fijo = new boolean[n];
        for (int i = 0; i < n; i++) {
            TableColumn<?, ?> c = tabla.getColumns().get(i);
            Object deseado = c.getProperties().get(ANCHO);
            ancho[i] = deseado instanceof Double ? (Double) deseado : c.getPrefWidth();
            minimo[i] = c.getMinWidth();
        }
        double[] resultado = new double[n];
        boolean reiniciar = true;
        while (reiniciar) {
            reiniciar = false;
            double ocupado = 0;
            double pesos = 0;
            for (int i = 0; i < n; i++) {
                if (fijo[i]) ocupado += resultado[i];
                else pesos += ancho[i];
            }
            double libre = Math.max(0, disponible - ocupado);
            for (int i = 0; i < n && !reiniciar; i++) {
                if (fijo[i]) continue;
                double w = pesos <= 0 ? 0 : libre * ancho[i] / pesos;
                if (w < minimo[i]) {
                    resultado[i] = minimo[i];
                    fijo[i] = true;
                    reiniciar = true;
                } else {
                    resultado[i] = w;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            TableColumn<?, ?> c = tabla.getColumns().get(i);
            if (Math.abs(c.getPrefWidth() - resultado[i]) > 0.5) c.setPrefWidth(resultado[i]);
        }
    }

    private static final String ANCHO = "anchoDeseado";

    private static Node vacio(String mensaje, String icono) {
        Label l = new Label(mensaje);
        l.getStyleClass().add("texto-muted");
        VBox caja = new VBox(8, Icons.crear(icono, 30), l);
        caja.setAlignment(Pos.CENTER);
        return caja;
    }

    public static <S> TableColumn<S, String> texto(String titulo, double ancho, Function<S, String> f) {
        TableColumn<S, String> c = new TableColumn<>(titulo);
        proporcional(c, ancho);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(nvl(f.apply(cd.getValue()))));
        c.setCellFactory(col -> new CeldaTexto<>(null, false));
        return c;
    }

    /** Celda de texto que muestra el contenido completo en un globo de ayuda cuando el texto queda recortado. */
    private static final class CeldaTexto<S> extends TableCell<S, String> {
        private final String alineacion;
        private final boolean negrita;

        CeldaTexto(String alineacion, boolean negrita) {
            this.alineacion = alineacion;
            this.negrita = negrita;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll("derecha", "centro");
            if (empty || item == null) {
                setText(null);
                setTooltip(null);
                setStyle("");
                return;
            }
            setText(item);
            if (alineacion != null) getStyleClass().add(alineacion);
            setStyle(negrita ? "-fx-font-weight: bold;" : "");
            setTooltip(item.length() > 10 ? new Tooltip(item) : null);
        }
    }

    public static <S> TableColumn<S, String> textoDerecha(String titulo, double ancho, Function<S, String> f) {
        return textoAlineado(titulo, ancho, f, "derecha");
    }

    /** Texto centrado (cantidades, mínimos…); el título de la columna se centra igual que sus datos. */
    public static <S> TableColumn<S, String> textoCentro(String titulo, double ancho, Function<S, String> f) {
        return textoAlineado(titulo, ancho, f, "centro");
    }

    private static <S> TableColumn<S, String> textoAlineado(String titulo, double ancho, Function<S, String> f, String alineacion) {
        TableColumn<S, String> c = texto(titulo, ancho, f);
        c.getStyleClass().add("col-" + alineacion);
        c.setCellFactory(col -> new CeldaTexto<>(alineacion, false));
        return c;
    }

    public static <S> TableColumn<S, String> negrita(String titulo, double ancho, Function<S, String> f) {
        TableColumn<S, String> c = texto(titulo, ancho, f);
        c.setCellFactory(col -> new CeldaTexto<>(null, true));
        return c;
    }

    /** Columna con un nodo cualquiera (insignias, botones, textos con estilo…). */
    public static <S> TableColumn<S, S> nodo(String titulo, double ancho, Function<S, Node> f) {
        return nodoAlineado(titulo, ancho, f, null);
    }

    /** Igual que {@link #nodo} pero centrado, con el título de la columna también centrado. */
    public static <S> TableColumn<S, S> nodoCentro(String titulo, double ancho, Function<S, Node> f) {
        return nodoAlineado(titulo, ancho, f, "centro");
    }

    private static <S> TableColumn<S, S> nodoAlineado(String titulo, double ancho, Function<S, Node> f, String alineacion) {
        TableColumn<S, S> c = new TableColumn<>(titulo);
        proporcional(c, ancho);
        c.setSortable(false);
        if (alineacion != null) c.getStyleClass().add("col-" + alineacion);
        c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        c.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(S item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                getStyleClass().remove("centro");
                if (alineacion != null && !empty) getStyleClass().add(alineacion);
                setGraphic(empty || item == null ? null : f.apply(item));
            }
        });
        return c;
    }

    private static void proporcional(TableColumn<?, ?> c, double ancho) {
        c.getProperties().put(ANCHO, ancho);
        c.setPrefWidth(ancho);
        // las columnas angostas (cifras, fechas, insignias) no se recortan; las anchas ceden espacio primero
        c.setMinWidth(ancho <= 160 ? ancho : Math.min(ancho * 0.55, 110));
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
