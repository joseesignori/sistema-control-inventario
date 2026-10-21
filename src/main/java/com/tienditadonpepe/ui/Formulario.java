package com.tienditadonpepe.ui;

import com.tienditadonpepe.util.NegocioException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.function.Function;

/**
 * Diálogo de formulario: campos con etiqueta, validación al guardar (los mensajes de {@link NegocioException}
 * se muestran dentro del diálogo y este permanece abierto) y botones Guardar / Cancelar.
 */
public class Formulario {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Dialog<ButtonType> dialogo = new Dialog<>();
    private final GridPane grid = new GridPane();
    private final Label lblError = new Label();
    private final VBox contenido = new VBox();
    private int fila = 0;
    private int columna = 0;
    private boolean completoSiguiente = false;
    private boolean guardado = false;

    public Formulario(String titulo, String subtitulo, double ancho) {
        Dialogs.estilizar(dialogo, titulo);
        grid.setHgap(14);
        grid.setVgap(14);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        lblError.getStyleClass().add("mensaje-error");
        lblError.setWrapText(true);
        lblError.setMaxWidth(Double.MAX_VALUE);
        lblError.setVisible(false);
        lblError.setManaged(false);
        VBox cuerpo = new VBox(14, grid, lblError);
        cuerpo.getStyleClass().add("dialog-cuerpo");
        contenido.getChildren().addAll(Dialogs.cabecera(titulo, subtitulo), cuerpo);
        contenido.setPrefWidth(ancho);
        dialogo.getDialogPane().setContent(contenido);
    }

    public Formulario(String titulo, String subtitulo) {
        this(titulo, subtitulo, 520);
    }

    /** El siguiente campo ocupará todo el ancho. */
    public Formulario completo() {
        completoSiguiente = true;
        return this;
    }

    /** Recuadro informativo (tipo: info, warn o error) que ocupa todo el ancho; devuelve la etiqueta para actualizarla. */
    public Label nota(String texto, String tipo) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        l.setMaxWidth(Double.MAX_VALUE);
        l.getStyleClass().add("mensaje-" + tipo);
        completoSiguiente = true;
        colocar(l);
        return l;
    }

    private void colocar(Node n) {
        if (completoSiguiente) {
            if (columna == 1) {
                fila++;
                columna = 0;
            }
            grid.add(n, 0, fila, 2, 1);
            fila++;
            columna = 0;
            completoSiguiente = false;
        } else {
            grid.add(n, columna, fila);
            if (columna == 0) {
                columna = 1;
            } else {
                columna = 0;
                fila++;
            }
        }
        GridPane.setHgrow(n, Priority.ALWAYS);
    }

    private <T extends Node> T campo(String etiqueta, T control, String ayuda) {
        Label l = new Label(etiqueta);
        l.getStyleClass().add("etiqueta-campo");
        VBox caja = new VBox(5, l, control);
        if (ayuda != null) {
            Label a = new Label(ayuda);
            a.setWrapText(true);
            a.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            a.getStyleClass().add("ayuda-campo");
            a.setWrapText(true);
            caja.getChildren().add(a);
        }
        if (control instanceof javafx.scene.layout.Region) ((javafx.scene.layout.Region) control).setMaxWidth(Double.MAX_VALUE);
        colocar(caja);
        return control;
    }

    public TextField texto(String etiqueta, String valor, String prompt) {
        return texto(etiqueta, valor, prompt, null);
    }

    public TextField texto(String etiqueta, String valor, String prompt, String ayuda) {
        TextField t = new TextField(valor == null ? "" : valor);
        t.setPromptText(prompt == null ? "" : prompt);
        return campo(etiqueta, t, ayuda);
    }

    public PasswordField clave(String etiqueta, String ayuda) {
        PasswordField p = new PasswordField();
        return campo(etiqueta, p, ayuda);
    }

    public TextArea area(String etiqueta, String valor, String prompt) {
        TextArea a = new TextArea(valor == null ? "" : valor);
        a.setPromptText(prompt == null ? "" : prompt);
        a.setPrefRowCount(3);
        a.setWrapText(true);
        return campo(etiqueta, a, null);
    }

    public <T> ComboBox<T> combo(String etiqueta, Collection<T> items, T seleccionado, Function<T, String> texto) {
        ComboBox<T> c = new ComboBox<>();
        c.getItems().addAll(items);
        c.setConverter(new StringConverter<>() {
            @Override
            public String toString(T o) {
                return o == null ? "" : texto.apply(o);
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });
        if (seleccionado != null) c.getSelectionModel().select(seleccionado);
        return campo(etiqueta, c, null);
    }

    public DatePicker fecha(String etiqueta, LocalDate valor, String ayuda) {
        DatePicker d = new DatePicker(valor);
        formatear(d);
        return campo(etiqueta, d, ayuda);
    }

    /** Muestra y acepta fechas como dd/mm/aaaa en un DatePicker (también al escribir a mano). */
    public static void formatear(DatePicker d) {
        d.setPromptText("dd/mm/aaaa");
        d.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate f) {
                return f == null ? "" : FMT.format(f);
            }

            @Override
            public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) return null;
                try {
                    return LocalDate.parse(s.trim(), FMT);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        d.getEditor().focusedProperty().addListener((o, a, enfocado) -> {
            if (!enfocado) d.setValue(d.getConverter().fromString(d.getEditor().getText()));
        });
    }

    public Dialog<ButtonType> dialogo() {
        return dialogo;
    }

    /** Muestra el diálogo y devuelve true si se guardó correctamente. */
    public boolean mostrar(String textoGuardar, Runnable guardar) {
        ButtonType ok = new ButtonType(textoGuardar, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(ok, cancelar);
        Dialogs.estilizarBoton(dialogo, ok, "btn-primary");
        Button boton = Dialogs.botonDe(dialogo, ok);
        boton.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                guardar.run();
                guardado = true;
            } catch (NegocioException e) {
                lblError.setText(e.getMessage());
                lblError.setVisible(true);
                lblError.setManaged(true);
                dialogo.getDialogPane().getScene().getWindow().sizeToScene();
                ev.consume();
            } catch (RuntimeException e) {
                e.printStackTrace();
                lblError.setText("No se pudo guardar: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
                lblError.setVisible(true);
                lblError.setManaged(true);
                dialogo.getDialogPane().getScene().getWindow().sizeToScene();
                ev.consume();
            }
        });
        dialogo.showAndWait();
        return guardado;
    }

    public static Insets relleno() {
        return new Insets(0);
    }

    /** Solo permite escribir dígitos y limita la longitud (DNI = 8, RUC = 11). */
    public static void soloDigitos(TextField campo, int max) {
        campo.setTextFormatter(new javafx.scene.control.TextFormatter<String>(ch ->
                ch.getControlNewText().matches("\\d{0," + max + "}") ? ch : null));
    }
}
