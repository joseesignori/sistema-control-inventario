package com.tienditadonpepe.controller;

import com.tienditadonpepe.App;
import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Usuario;
import com.tienditadonpepe.service.AuthService;
import com.tienditadonpepe.service.InventarioService;
import com.tienditadonpepe.service.SesionUsuario;
import com.tienditadonpepe.ui.Dialogs;
import com.tienditadonpepe.ui.Formulario;
import com.tienditadonpepe.ui.Icons;
import com.tienditadonpepe.ui.Logo;
import com.tienditadonpepe.ui.Tareas;
import com.tienditadonpepe.ui.Toast;
import com.tienditadonpepe.ui.Vistas;
import com.tienditadonpepe.util.Fechas;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ventana principal: barra lateral con las opciones según el rol, cabecera con alertas y área de contenido. */
public class MainController {

    private static MainController instancia;

    private static final class Item {
        final String clave;
        final String titulo;
        final String subtitulo;
        final String icono;
        final String fxml;
        final boolean soloAdmin;

        Item(String clave, String titulo, String subtitulo, String icono, String fxml, boolean soloAdmin) {
            this.clave = clave;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.icono = icono;
            this.fxml = fxml;
            this.soloAdmin = soloAdmin;
        }
    }

    @FXML private VBox sidebar;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblFecha;
    @FXML private StackPane campana;
    @FXML private StackPane contenido;

    private final Map<String, Item> items = new LinkedHashMap<>();
    private final Map<String, Button> botones = new LinkedHashMap<>();
    private final InventarioService inventario = new InventarioService();
    private InventarioService.Alertas alertas;
    private Popup popup;
    private String claveActual;

    @FXML
    private void initialize() {
        instancia = this;
    }

    /** Se invoca una vez cargada la ventana, con la sesión ya iniciada. */
    public void iniciar() {
        Usuario u = SesionUsuario.getInstance().exigir();
        items.put("panel", new Item("panel", "Panel principal", "Resumen del inventario y de las ventas", "inicio", "dashboard", false));
        items.put("productos", new Item("productos", "Productos y existencias", "Cantidad disponible, stock mínimo y vencimientos", "caja", "productos", false));
        construirBarraLateral(u);
        lblFecha.setText(Fechas.fechaLarga(LocalDate.now()));
        construirCampana();
        // Actualiza las alertas cada minuto (por ejemplo, tras ventas hechas desde otra caja).
        Timeline reloj = new Timeline(new KeyFrame(Duration.minutes(1), e -> refrescarAlertas()));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
        navegar("panel");
    }

    // ---- Barra lateral -------------------------------------------------------------------------

    private void construirBarraLateral(Usuario u) {
        Label pre = new Label("TIENDITA DE");
        pre.getStyleClass().add("sidebar-nombre-pre");
        Label nombre = new Label("Don Pepe");
        nombre.getStyleClass().add("sidebar-nombre");
        Label lema = new Label("Control de inventario");
        lema.getStyleClass().add("sidebar-lema");
        HBox marca = new HBox(12, Logo.crear(42), new VBox(0, pre, nombre, lema));
        marca.getStyleClass().add("sidebar-marca");
        sidebar.getChildren().add(marca);

        seccion("OPERACIÓN", "panel");
        seccion("INVENTARIO", "productos");

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);
        sidebar.getChildren().add(espacio);
        sidebar.getChildren().add(tarjetaUsuario(u));
    }

    private void seccion(String titulo, String... claves) {
        Usuario u = SesionUsuario.getInstance().getUsuario();
        boolean alguno = false;
        VBox grupo = new VBox(3);
        grupo.setPadding(new Insets(0, 14, 0, 14));
        for (String clave : claves) {
            Item it = items.get(clave);
            if (it.soloAdmin && !u.esAdministrador()) continue;
            Button b = new Button(shortTitle(it), Icons.crear(it.icono, 18));
            b.getStyleClass().add("nav-btn");
            b.setOnAction(e -> navegar(clave));
            botones.put(clave, b);
            grupo.getChildren().add(b);
            alguno = true;
        }
        if (!alguno) return;
        Label l = new Label(titulo);
        l.getStyleClass().add("sidebar-seccion");
        sidebar.getChildren().addAll(l, grupo);
    }

    private static String shortTitle(Item it) {
        switch (it.clave) {
            case "panel": return "Panel principal";
            case "productos": return "Productos y stock";
            case "movimientos": return "Movimientos";
            case "reportes": return "Reportes";
            default: return it.titulo;
        }
    }

    private Node tarjetaUsuario(Usuario u) {
        Label iniciales = new Label(u.iniciales());
        StackPane avatar = new StackPane(iniciales);
        avatar.getStyleClass().add("avatar");
        Label nombre = new Label(u.getNombre_completo());
        nombre.getStyleClass().add("usuario-nombre");
        nombre.setMaxWidth(150);
        Label rol = new Label(u.getRol());
        rol.getStyleClass().add("usuario-rol");
        VBox textos = new VBox(1, nombre, rol);
        HBox.setHgrow(textos, Priority.ALWAYS);
        HBox fila = new HBox(10, avatar, textos);
        fila.setAlignment(Pos.CENTER_LEFT);
        Button clave = new Button("Contraseña", Icons.crear("candado", 15));
        clave.getStyleClass().add("sidebar-accion");
        clave.setMaxWidth(Double.MAX_VALUE);
        clave.setOnAction(e -> cambiarContrasena());
        Button salir = new Button("Salir", Icons.crear("salir", 15));
        salir.getStyleClass().add("sidebar-accion");
        salir.setMaxWidth(Double.MAX_VALUE);
        salir.setOnAction(e -> cerrarSesion());
        HBox.setHgrow(clave, Priority.ALWAYS);
        HBox.setHgrow(salir, Priority.ALWAYS);
        HBox acciones = new HBox(6, clave, salir);
        VBox caja = new VBox(10, fila, acciones);
        caja.getStyleClass().add("sidebar-usuario");
        return caja;
    }

    // ---- Navegación ------------------------------------------------------------------------------

    public void navegar(String clave) {
        Item it = items.get(clave);
        if (it == null) return;
        claveActual = clave;
        botones.forEach((k, b) -> {
            b.getStyleClass().remove("activo");
            if (k.equals(clave)) b.getStyleClass().add("activo");
        });
        lblTitulo.setText(it.titulo);
        lblSubtitulo.setText(it.subtitulo);
        Vistas.Cargada<Object> v = Vistas.cargar(it.fxml);
        ScrollPane sp = new ScrollPane(v.raiz);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contenido.getChildren().setAll(sp);
        refrescarAlertas();
    }

    /** Permite que otras vistas naveguen (por ejemplo, desde una alerta del panel). */
    public static void ir(String clave) {
        if (instancia != null) instancia.navegar(clave);
    }

    // ---- Alertas (campana) -------------------------------------------------------------------------

    private void construirCampana() {
        Button b = new Button(null, Icons.crear("campana", 20));
        b.getStyleClass().add("campana");
        b.setTooltip(new Tooltip("Alertas de stock y vencimiento"));
        b.setOnAction(e -> mostrarAlertas(b));
        campana.getChildren().setAll(b);
    }

    public static void refrescarAlertas() {
        if (instancia != null) instancia.cargarAlertas();
    }

    private void cargarAlertas() {
        if (!SesionUsuario.getInstance().haySesion()) return;
        Tareas.ejecutar(inventario::obtenerAlertas, a -> {
            alertas = a;
            int n = a.stockCritico.size() + a.porVencer.size();
            Node boton = campana.getChildren().get(0);
            campana.getChildren().setAll(boton);
            if (n > 0) {
                Label c = new Label(String.valueOf(n));
                c.getStyleClass().add("campana-contador");
                c.setMouseTransparent(true);
                StackPane.setAlignment(c, Pos.TOP_RIGHT);
                StackPane.setMargin(c, new Insets(-4, -4, 0, 0));
                campana.getChildren().add(c);
            }
        }, t -> { /* las alertas son secundarias: no interrumpen al usuario */ });
    }

    private void mostrarAlertas(Node ancla) {
        if (popup != null && popup.isShowing()) {
            popup.hide();
            return;
        }
        VBox caja = new VBox(10);
        caja.getStyleClass().add("popover");
        caja.setPrefWidth(380);
        Label t = new Label("Alertas del inventario");
        t.getStyleClass().add("card-titulo");
        caja.getChildren().add(t);
        if (alertas == null || !alertas.hayAlertas()) {
            Label ok = new Label("Todo en orden: no hay productos en stock crítico ni próximos a vencer.");
            ok.setWrapText(true);
            ok.getStyleClass().addAll("alerta-fila", "alerta-ok");
            caja.getChildren().add(ok);
        } else {
            for (Producto p : alertas.stockCritico) {
                caja.getChildren().add(filaAlerta(p, "danger", "Stock crítico",
                        p.getStock_actual() + " latas (mínimo " + p.getStock_minimo() + ")"));
            }
            for (Producto p : alertas.porVencer) {
                String det = p.estaVencido() ? "Vencido el " + Fechas.fecha(p.getFecha_vencimiento())
                        : "Vence el " + Fechas.fecha(p.getFecha_vencimiento()) + " (en " + p.diasParaVencer() + " días)";
                caja.getChildren().add(filaAlerta(p, "warn", "Próximo a vencer", det));
            }
        }
        Button ver = new Button("Ver productos y stock");
        ver.getStyleClass().add("btn-ghost");
        ver.setOnAction(e -> {
            popup.hide();
            navegar("productos");
        });
        caja.getChildren().add(ver);
        popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(caja);
        javafx.geometry.Bounds b = ancla.localToScreen(ancla.getBoundsInLocal());
        popup.show(ancla, b.getMaxX() - 380, b.getMaxY() + 8);
    }

    private HBox filaAlerta(Producto p, String tipo, String titulo, String detalle) {
        Label t = new Label(titulo + " · " + p.nombreCompleto() + " " + p.getMarca());
        t.getStyleClass().add("alerta-titulo");
        t.setWrapText(true);
        t.setMaxWidth(290);
        Label d = new Label(detalle);
        d.getStyleClass().add("alerta-detalle");
        HBox fila = new HBox(12, Icons.crear("alerta", 20, "danger".equals(tipo) ? "icono-danger" : "icono-warn"), new VBox(2, t, d));
        fila.getStyleClass().addAll("alerta-fila", "alerta-" + tipo);
        return fila;
    }

    // ---- Sesión -----------------------------------------------------------------------------------------

    private void cerrarSesion() {
        if (Dialogs.confirmar("Cerrar sesión", "¿Deseas salir del sistema? Tendrás que ingresar nuevamente tu usuario y contraseña.",
                "Cerrar sesión", false)) {
            new AuthService().cerrarSesion();
            instancia = null;
            App.mostrarLogin();
        }
    }

    private void cambiarContrasena() {
        Formulario f = new Formulario("Cambiar contraseña", "Elige una contraseña de al menos 8 caracteres con letras y números.", 440);
        f.completo();
        PasswordField actual = f.clave("Contraseña actual", null);
        f.completo();
        PasswordField nueva = f.clave("Nueva contraseña", null);
        f.completo();
        PasswordField confirmar = f.clave("Confirmar nueva contraseña", null);
        boolean ok = f.mostrar("Cambiar contraseña", () -> new AuthService().cambiarContrasena(actual.getText(), nueva.getText(), confirmar.getText()));
        if (ok) Toast.ok("Contraseña actualizada correctamente.");
    }
}
