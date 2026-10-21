package com.tienditadonpepe.ui;

import com.tienditadonpepe.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/** Cuadros de diálogo con el estilo de la aplicación. */
public final class Dialogs {

    private Dialogs() {
    }

    /** Aplica el estilo común y fija la ventana propietaria. */
    public static void estilizar(Dialog<?> d, String titulo) {
        d.setTitle(titulo);
        d.setHeaderText(null);
        d.setGraphic(null);
        d.getDialogPane().getStylesheets().add(Vistas.CSS);
        Stage owner = App.getStage();
        if (owner != null && owner.isShowing()) {
            d.initOwner(owner);
        }
        d.setOnShowing(e -> permitirCierreConX(d));
        d.setOnShown(e -> {
            Stage s = (Stage) d.getDialogPane().getScene().getWindow();
            if (App.icono() != null) s.getIcons().add(App.icono());
        });
    }

    public static void estilizarBoton(Dialog<?> d, ButtonType tipo, String clase) {
        Node n = d.getDialogPane().lookupButton(tipo);
        if (n != null) n.getStyleClass().add(clase);
    }

    private static Node icono(String nombre, String tipo) {
        StackPane p = new StackPane(Icons.crear(nombre, 22));
        p.getStyleClass().addAll("dialog-icono", "dialog-icono-" + tipo);
        p.setMinSize(44, 44);
        p.setPrefSize(44, 44);
        p.setMaxSize(44, 44);
        return p;
    }

    private static Dialog<ButtonType> mensaje(String titulo, String texto, String iconoNombre, String tipo,
                                              ButtonType... botones) {
        Dialog<ButtonType> d = new Dialog<>();
        estilizar(d, titulo);
        Label t = new Label(titulo);
        t.getStyleClass().add("dialog-titulo");
        Label m = new Label(texto);
        m.setWrapText(true);
        m.setMaxWidth(340);
        m.getStyleClass().add("texto-muted");
        VBox textos = new VBox(6, t, m);
        HBox.setHgrow(textos, Priority.ALWAYS);
        HBox cuerpo = new HBox(16, icono(iconoNombre, tipo), textos);
        cuerpo.setAlignment(Pos.TOP_LEFT);
        cuerpo.setPadding(new Insets(24, 26, 20, 26));
        cuerpo.setMinWidth(440);
        d.getDialogPane().setContent(cuerpo);
        d.getDialogPane().getButtonTypes().addAll(botones);
        return d;
    }

    public static boolean confirmar(String titulo, String texto, String textoAceptar, boolean peligro) {
        ButtonType si = new ButtonType(textoAceptar, ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> d = mensaje(titulo, texto, peligro ? "alerta" : "info", peligro ? "danger" : "info", si, no);
        estilizarBoton(d, si, peligro ? "btn-danger-solid" : "btn-primary");
        Optional<ButtonType> r = d.showAndWait();
        return r.isPresent() && r.get() == si;
    }

    public static void info(String titulo, String texto) {
        ButtonType ok = new ButtonType("Entendido", ButtonBar.ButtonData.OK_DONE);
        Dialog<ButtonType> d = mensaje(titulo, texto, "info", "info", ok);
        estilizarBoton(d, ok, "btn-primary");
        d.showAndWait();
    }

    public static void error(String titulo, String texto) {
        ButtonType ok = new ButtonType("Cerrar", ButtonBar.ButtonData.OK_DONE);
        Dialog<ButtonType> d = mensaje(titulo, texto, "alerta", "danger", ok);
        estilizarBoton(d, ok, "btn-primary");
        d.showAndWait();
    }

    /** Cabecera estándar para diálogos con formulario. */
    public static VBox cabecera(String titulo, String subtitulo) {
        Label t = new Label(titulo);
        t.getStyleClass().add("dialog-titulo");
        VBox v = new VBox(4, t);
        if (subtitulo != null && !subtitulo.isEmpty()) {
            Label s = new Label(subtitulo);
            s.setWrapText(true);
            s.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            s.setMaxWidth(470);
            s.getStyleClass().add("texto-muted");
            v.getChildren().add(s);
        }
        v.getStyleClass().add("dialog-cabecera");
        return v;
    }

    public static Button botonDe(Dialog<?> d, ButtonType t) {
        return (Button) d.getDialogPane().lookupButton(t);
    }

    /**
     * JavaFX solo deja cerrar un diálogo con la «X» de la ventana si tiene un botón de tipo CANCEL_CLOSE.
     * Se agrega uno invisible para que la «X» funcione en diálogos que solo tienen botones de acción.
     */
    public static void permitirCierreConX(Dialog<?> d) {
        for (ButtonType t : d.getDialogPane().getButtonTypes()) {
            if (t.getButtonData().isCancelButton()) return;
        }
        ButtonType cierre = new ButtonType("Cerrar ventana", ButtonBar.ButtonData.CANCEL_CLOSE);
        d.getDialogPane().getButtonTypes().add(cierre);
        Node b = d.getDialogPane().lookupButton(cierre);
        b.setVisible(false);
        b.setManaged(false);
    }
}
