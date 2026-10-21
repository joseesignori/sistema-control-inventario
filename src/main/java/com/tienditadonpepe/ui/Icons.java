package com.tienditadonpepe.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.Map;

/** Iconos vectoriales de trazo (cuadrícula de 24 × 24) dibujados con SVGPath; el color se controla desde el CSS (.icono). */
public final class Icons {

    private static final Map<String, String> RUTAS = new HashMap<>();

    static {
        RUTAS.put("inicio", "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z M9 22V12h6v10");
        RUTAS.put("caja", "M16.5 9.4l-9-5.19 M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.27 6.96L12 12.01l8.73-5.05 M12 22.08V12");
        RUTAS.put("carrito", "M9 22a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M20 22a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6");
        RUTAS.put("reloj", "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z M12 6v6l4 2");
        RUTAS.put("usuarios", "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75");
        RUTAS.put("usuario", "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z");
        RUTAS.put("camion", "M1 3h15v13H1z M16 8h4l3 3v5h-7V8z M5.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z M18.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z");
        RUTAS.put("grafico", "M18 20V10 M12 20V4 M6 20v-6");
        RUTAS.put("salir", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9");
        RUTAS.put("buscar", "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z M21 21l-4.35-4.35");
        RUTAS.put("mas", "M12 5v14 M5 12h14");
        RUTAS.put("menos", "M5 12h14");
        RUTAS.put("alerta", "M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z M12 9v4 M12 17h.01");
        RUTAS.put("papelera", "M3 6h18 M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2");
        RUTAS.put("editar", "M12 20h9 M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z");
        RUTAS.put("descargar", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4 M7 10l5 5 5-5 M12 15V3");
        RUTAS.put("imprimir", "M6 9V2h12v7 M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2 M6 14h12v8H6z");
        RUTAS.put("candado", "M5 11h14v10H5z M7 11V7a5 5 0 0 1 10 0v4");
        RUTAS.put("check", "M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4L12 14.01l-3-3");
        RUTAS.put("cerrar", "M18 6L6 18 M6 6l12 12");
        RUTAS.put("sube", "M12 19V5 M5 12l7-7 7 7");
        RUTAS.put("baja", "M12 5v14 M19 12l-7 7-7-7");
        RUTAS.put("refrescar", "M23 4v6h-6 M1 20v-6h6 M3.51 9a9 9 0 0 1 14.85-3.36L23 10 M1 14l4.64 4.36A9 9 0 0 0 20.49 15");
        RUTAS.put("documento", "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8");
        RUTAS.put("calendario", "M19 4H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z M16 2v4 M8 2v4 M3 10h18");
        RUTAS.put("ojo", "M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z");
        RUTAS.put("ojo-tachado", "M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94 M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19 M14.12 14.12a3 3 0 1 1-4.24-4.24 M1 1l22 22");
        RUTAS.put("prohibido", "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z M4.93 4.93l14.14 14.14");
        RUTAS.put("dinero", "M12 1v22 M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6");
        RUTAS.put("tendencia", "M23 6l-9.5 9.5-5-5L1 18 M17 6h6v6");
        RUTAS.put("campana", "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9 M13.73 21a2 2 0 0 1-3.46 0");
        RUTAS.put("escudo", "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z");
        RUTAS.put("info", "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z M12 16v-4 M12 8h.01");
        RUTAS.put("etiqueta", "M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z M7 7h.01");
        RUTAS.put("capas", "M12 2L2 7l10 5 10-5-10-5z M2 17l10 5 10-5 M2 12l10 5 10-5");
        RUTAS.put("llave", "M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.78 7.78 5.5 5.5 0 0 1 7.78-7.78zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4");
        RUTAS.put("ajustes", "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z");
    }

    private Icons() {
    }

    /** Crea un icono del tamaño indicado (px). El nombre debe existir en el catálogo. */
    public static Node crear(String nombre, double tam) {
        String ruta = RUTAS.get(nombre);
        if (ruta == null) throw new IllegalArgumentException("Icono desconocido: " + nombre);
        SVGPath p = new SVGPath();
        p.setContent(ruta);
        p.getStyleClass().add("icono");
        Group g = new Group(p);
        double escala = tam / 24.0;
        g.setScaleX(escala);
        g.setScaleY(escala);
        StackPane caja = new StackPane(g);
        caja.setMinSize(tam, tam);
        caja.setPrefSize(tam, tam);
        caja.setMaxSize(tam, tam);
        caja.setMouseTransparent(true);
        return caja;
    }

    /** Icono con una clase de color adicional (icono-primario, icono-ok, icono-warn, icono-danger, icono-blanco). */
    public static Node crear(String nombre, double tam, String claseColor) {
        Node n = crear(nombre, tam);
        ((Group) ((StackPane) n).getChildren().get(0)).getChildren().get(0).getStyleClass().add(claseColor);
        return n;
    }

    public static <T extends Labeled> T en(T control, String nombre, double tam) {
        control.setGraphic(crear(nombre, tam));
        return control;
    }
}
