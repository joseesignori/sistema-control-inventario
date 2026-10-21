package com.tienditadonpepe;

/** Clase de arranque separada de Application para poder ejecutar el JAR sin el módulo JavaFX en el module-path. */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(App.class, args);
    }
}
