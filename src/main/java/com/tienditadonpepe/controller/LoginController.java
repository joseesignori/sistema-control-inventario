package com.tienditadonpepe.controller;

import com.tienditadonpepe.App;
import com.tienditadonpepe.service.AuthService;
import com.tienditadonpepe.ui.Icons;
import com.tienditadonpepe.ui.Logo;
import com.tienditadonpepe.util.NegocioException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** RF01 – Pantalla de inicio de sesión con usuario y contraseña. */
public class LoginController {

    @FXML private VBox marca;
    @FXML private Label lblError;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private TextField txtClaveVisible;
    @FXML private Button btnVer;
    @FXML private Button btnIngresar;

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
        Label nombre = new Label(App.NOMBRE);
        nombre.getStyleClass().add("login-marca");
        nombre.setWrapText(true);
        nombre.setAlignment(Pos.CENTER);
        nombre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        nombre.setMaxWidth(380);
        marca.getChildren().setAll(Logo.crear(96), nombre);

        btnVer.setGraphic(Icons.crear("ojo", 18));
        txtClaveVisible.textProperty().bindBidirectional(txtClave.textProperty());
        btnVer.setOnAction(e -> {
            boolean mostrar = !txtClaveVisible.isVisible();
            txtClaveVisible.setVisible(mostrar);
            txtClave.setVisible(!mostrar);
            btnVer.setGraphic(Icons.crear(mostrar ? "ojo-tachado" : "ojo", 18));
            (mostrar ? txtClaveVisible : txtClave).requestFocus();
        });
        txtUsuario.setOnAction(e -> txtClave.requestFocus());
        txtClaveVisible.setOnAction(e -> ingresar());
    }

    public void enfocar() {
        Platform.runLater(() -> txtUsuario.requestFocus());
    }

    @FXML
    private void ingresar() {
        ocultarError();
        String usuario = txtUsuario.getText();
        String clave = txtClave.getText();
        if (usuario == null || usuario.trim().isEmpty()) {
            error("Ingresa tu usuario.");
            txtUsuario.requestFocus();
            return;
        }
        if (clave == null || clave.isEmpty()) {
            error("Ingresa tu contraseña.");
            txtClave.requestFocus();
            return;
        }
        btnIngresar.setDisable(true);
        btnIngresar.setText("Verificando…");
        com.tienditadonpepe.ui.Tareas.ejecutar(() -> auth.iniciarSesion(usuario, clave), u -> App.mostrarPrincipal(), t -> {
            btnIngresar.setDisable(false);
            btnIngresar.setText("Ingresar");
            if (t instanceof NegocioException) {
                error(t.getMessage());
            } else {
                error("No se pudo iniciar sesión. Verifica que MySQL esté encendido.");
                t.printStackTrace();
            }
            txtClave.clear();
            txtClave.requestFocus();
        });
    }

    private void error(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
