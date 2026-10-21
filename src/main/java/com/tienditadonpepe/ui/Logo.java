package com.tienditadonpepe.ui;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineJoin;

/** Marca de la tienda: una lata de atún sobre fondo ámbar (dibujo propio, sin imágenes externas). */
public final class Logo {

    private Logo() {
    }

    public static Node crear(double tam) {
        Rectangle fondo = new Rectangle(0, 0, 40, 40);
        fondo.setArcWidth(12);
        fondo.setArcHeight(12);
        fondo.setFill(Color.web("#F2A100"));

        Color tinta = Color.web("#0C2B40");
        SVGPath cuerpo = new SVGPath();
        cuerpo.setContent("M9 14v12a11 4.2 0 0 0 22 0V14z");
        cuerpo.setFill(Color.WHITE);
        cuerpo.setStroke(tinta);
        cuerpo.setStrokeWidth(1.8);
        cuerpo.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Rectangle etiqueta = new Rectangle(9.9, 18.4, 20.2, 5.6);
        etiqueta.setFill(Color.web("#0B5C8E"));

        SVGPath pez = new SVGPath();
        pez.setContent("M14.5 21.2c2-1.6 4.3-1.6 6.2 0-1.9 1.6-4.2 1.6-6.2 0z M20.7 21.2l2.2-1.3v2.6z");
        pez.setFill(Color.WHITE);

        Ellipse tapa = new Ellipse(20, 14, 11, 4.2);
        tapa.setFill(Color.web("#FFE8B0"));
        tapa.setStroke(tinta);
        tapa.setStrokeWidth(1.8);

        Ellipse brillo = new Ellipse(20, 14, 6.5, 2);
        brillo.setFill(Color.TRANSPARENT);
        brillo.setStroke(Color.web("#E0A93A"));
        brillo.setStrokeWidth(1);

        Group dibujo = new Group(fondo, cuerpo, etiqueta, pez, tapa, brillo);
        double escala = tam / 40.0;
        dibujo.setScaleX(escala);
        dibujo.setScaleY(escala);
        StackPane caja = new StackPane(dibujo);
        caja.setPadding(Insets.EMPTY);
        caja.setMinSize(tam, tam);
        caja.setPrefSize(tam, tam);
        caja.setMaxSize(tam, tam);
        return caja;
    }

    /** Imagen para el icono de la ventana. */
    public static Image imagen(int tam) {
        try {
            Node n = crear(tam);
            SnapshotParameters p = new SnapshotParameters();
            p.setFill(Color.TRANSPARENT);
            return n.snapshot(p, null);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
