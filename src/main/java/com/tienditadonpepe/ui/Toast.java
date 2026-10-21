package com.tienditadonpepe.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/** Avisos breves no bloqueantes (esquina inferior derecha). */
public final class Toast {

    private static StackPane host;

    private Toast() {
    }

    /** Define el contenedor sobre el que se muestran los avisos (raíz de la ventana actual). */
    public static void setHost(StackPane contenedor) {
        host = contenedor;
    }

    public static void ok(String mensaje) {
        mostrar(mensaje, "toast-ok", "check");
    }

    public static void error(String mensaje) {
        mostrar(mensaje, "toast-error", "alerta");
    }

    public static void info(String mensaje) {
        mostrar(mensaje, "toast-info", "info");
    }

    private static void mostrar(String mensaje, String clase, String icono) {
        if (host == null) return;
        Runnable r = () -> {
            Label l = new Label(mensaje);
            l.setWrapText(true);
            l.setMaxWidth(420);
            HBox caja = new HBox(10, Icons.crear(icono, 18), l);
            caja.getStyleClass().addAll("toast", clase);
            caja.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
            StackPane.setAlignment(caja, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(caja, new Insets(0, 26, 26, 0));
            caja.setMouseTransparent(true);
            host.getChildren().add(caja);
            FadeTransition entra = new FadeTransition(Duration.millis(180), caja);
            entra.setFromValue(0);
            entra.setToValue(1);
            PauseTransition espera = new PauseTransition(Duration.seconds(clase.equals("toast-error") ? 5 : 3));
            FadeTransition sale = new FadeTransition(Duration.millis(300), caja);
            sale.setToValue(0);
            SequentialTransition seq = new SequentialTransition(entra, espera, sale);
            seq.setOnFinished(e -> host.getChildren().remove(caja));
            seq.play();
        };
        if (javafx.application.Platform.isFxApplicationThread()) r.run();
        else javafx.application.Platform.runLater(r);
    }
}
