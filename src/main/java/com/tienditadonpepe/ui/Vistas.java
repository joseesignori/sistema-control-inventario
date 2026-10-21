package com.tienditadonpepe.ui;

import com.tienditadonpepe.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Carga de vistas FXML y estilos comunes. */
public final class Vistas {

    public static final String CSS = Vistas.class.getResource("/css/app.css").toExternalForm();

    /** Vista cargada junto con su controlador. */
    public static final class Cargada<C> {
        public final Parent raiz;
        public final C controlador;

        Cargada(Parent raiz, C controlador) {
            this.raiz = raiz;
            this.controlador = controlador;
        }
    }

    private Vistas() {
    }

    public static <C> Cargada<C> cargar(String nombreFxml) {
        FXMLLoader loader = new FXMLLoader(Vistas.class.getResource("/fxml/" + nombreFxml + ".fxml"));
        try {
            Parent raiz = loader.load();
            fijarBotones(raiz);
            return new Cargada<>(raiz, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la vista " + nombreFxml, e);
        }
    }

    public static Scene escena(Parent raiz, double ancho, double alto) {
        Scene s = new Scene(raiz, ancho, alto);
        s.getStylesheets().add(CSS);
        return s;
    }

    /** Evita que los botones se trunquen («Registr...») cuando la barra de herramientas se estrecha. */
    public static void fijarBotones(javafx.scene.Node raiz) {
        for (javafx.scene.Node n : raiz.lookupAll(".button")) {
            if (n instanceof javafx.scene.control.Button) ((javafx.scene.control.Button) n).setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        }
    }

    public static javafx.stage.Stage ventana() {
        return App.getStage();
    }
}
