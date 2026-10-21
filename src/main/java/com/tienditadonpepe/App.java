package com.tienditadonpepe;

import com.tienditadonpepe.config.AppConfig;
import com.tienditadonpepe.config.ConexionBD;
import com.tienditadonpepe.config.DatabaseInitializer;
import com.tienditadonpepe.controller.LoginController;
import com.tienditadonpepe.controller.MainController;
import com.tienditadonpepe.ui.Icons;
import com.tienditadonpepe.ui.Logo;
import com.tienditadonpepe.ui.Tareas;
import com.tienditadonpepe.ui.Toast;
import com.tienditadonpepe.ui.Vistas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX. Prepara la base de datos (primer arranque), muestra el inicio de sesión
 * y, tras autenticarse, la ventana principal con las funciones según el rol del usuario.
 */
public class App extends Application {

    public static final String NOMBRE = "Tiendita de Don Pepe";

    private static Stage stage;
    private static Image icono;

    public static Stage getStage() {
        return stage;
    }

    public static Image icono() {
        return icono;
    }

    @Override
    public void start(Stage primaria) {
        stage = primaria;
        icono = Logo.imagen(64);
        if (icono != null) stage.getIcons().add(icono);
        stage.setTitle(NOMBRE + " — Control de inventario de atún enlatado");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        javafx.geometry.Rectangle2D pantalla = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setWidth(Math.max(1100, Math.min(1440, pantalla.getWidth() * 0.92)));
        stage.setHeight(Math.max(700, Math.min(880, pantalla.getHeight() * 0.92)));
        mostrarCarga("Preparando el sistema…");
        stage.show();
        stage.centerOnScreen();
        iniciarBaseDeDatos();
    }

    private void iniciarBaseDeDatos() {
        Tareas.ejecutar(DatabaseInitializer::asegurarBaseDeDatos, r -> {
            if (r.ok) mostrarLogin();
            else mostrarErrorConexion(r.mensaje);
        }, t -> mostrarErrorConexion(t.getMessage()));
    }

    // ---- Pantallas ---------------------------------------------------------------------------

    public static void mostrarLogin() {
        Vistas.Cargada<LoginController> v = Vistas.cargar("login");
        cambiarRaiz(v.raiz);
        v.controlador.enfocar();
    }

    public static void mostrarPrincipal() {
        Vistas.Cargada<MainController> v = Vistas.cargar("main");
        cambiarRaiz(v.raiz);
        v.controlador.iniciar();
    }

    private void mostrarCarga(String texto) {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(34, 34);
        Label titulo = new Label(NOMBRE);
        titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label sub = new Label(texto);
        sub.setStyle("-fx-text-fill: #CFE3F1;");
        VBox caja = new VBox(14, Logo.crear(64), titulo, sub, pi);
        caja.setAlignment(Pos.CENTER);
        caja.getStyleClass().add("pantalla-carga");
        cambiarRaiz(caja);
    }

    private void mostrarErrorConexion(String mensaje) {
        Label titulo = new Label("No se pudo preparar la base de datos");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label detalle = new Label(mensaje == null ? "Error desconocido." : mensaje);
        detalle.setWrapText(true);
        detalle.setMaxWidth(520);
        Label ayuda = new Label("Servidor: " + AppConfig.dbHost() + ":" + AppConfig.dbPort() + "   ·   Base de datos: " + AppConfig.dbName()
                + "   ·   Usuario: " + AppConfig.dbUser() + "\nPuedes cambiar estos valores en el archivo db.properties.");
        ayuda.getStyleClass().addAll("texto-muted", "texto-peq");
        ayuda.setWrapText(true);
        ayuda.setMaxWidth(520);
        Button reintentar = Icons.en(new Button("Reintentar"), "refrescar", 16);
        reintentar.getStyleClass().add("btn-primary");
        reintentar.setOnAction(e -> {
            mostrarCarga("Conectando con MySQL…");
            iniciarBaseDeDatos();
        });
        Button salir = new Button("Salir");
        salir.setOnAction(e -> Platform.exit());
        HBox botones = new HBox(10, reintentar, salir);
        VBox tarjeta = new VBox(14, Icons.crear("alerta", 34, "icono-danger"), titulo, detalle, ayuda, botones);
        tarjeta.getStyleClass().addAll("card", "card-pad");
        tarjeta.setMaxWidth(580);
        tarjeta.setMaxHeight(VBox.USE_PREF_SIZE);
        tarjeta.setStyle("-fx-padding: 30; -fx-spacing: 14;");
        StackPane fondo = new StackPane(tarjeta);
        fondo.setAlignment(Pos.CENTER);
        cambiarRaiz(fondo);
    }

    private static void cambiarRaiz(Parent contenido) {
        StackPane raiz = contenido instanceof StackPane ? (StackPane) contenido : new StackPane(contenido);
        Toast.setHost(raiz);
        Scene s = stage.getScene();
        if (s == null) {
            stage.setScene(Vistas.escena(raiz, stage.getWidth(), stage.getHeight()));
        } else {
            s.setRoot(raiz);
        }
    }

    @Override
    public void stop() {
        ConexionBD.getInstance().cerrar();
    }
}
