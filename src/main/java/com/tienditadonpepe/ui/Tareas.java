package com.tienditadonpepe.ui;

import com.tienditadonpepe.util.NegocioException;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Ejecuta consultas a la base de datos fuera del hilo de la interfaz para que la ventana nunca se congele. */
public final class Tareas {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "tiendita-tarea");
        t.setDaemon(true);
        return t;
    });

    private Tareas() {
    }

    /** Ejecuta el trabajo en segundo plano; el resultado o el error se entregan en el hilo de JavaFX. */
    public static <T> void ejecutar(Supplier<T> trabajo, Consumer<T> alExito, Consumer<Throwable> alError) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() {
                return trabajo.get();
            }
        };
        tarea.setOnSucceeded(e -> alExito.accept(tarea.getValue()));
        tarea.setOnFailed(e -> alError.accept(tarea.getException()));
        POOL.execute(tarea);
    }

    /** Variante con aviso de error estándar. */
    public static <T> void cargar(Supplier<T> trabajo, Consumer<T> alExito) {
        ejecutar(trabajo, alExito, Tareas::mostrarError);
    }

    public static void mostrarError(Throwable t) {
        String mensaje;
        if (t instanceof NegocioException) mensaje = t.getMessage();
        else {
            mensaje = "Ocurrió un problema inesperado: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
            t.printStackTrace();
        }
        if (Platform.isFxApplicationThread()) Toast.error(mensaje);
        else Platform.runLater(() -> Toast.error(mensaje));
    }
}
