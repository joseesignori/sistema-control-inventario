package com.tienditadonpepe.controller;

import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Proveedor;
import com.tienditadonpepe.service.InventarioService;
import com.tienditadonpepe.service.ProductoService;
import com.tienditadonpepe.service.ProveedorService;
import com.tienditadonpepe.service.SesionUsuario;
import com.tienditadonpepe.ui.Dialogs;
import com.tienditadonpepe.ui.Formulario;
import com.tienditadonpepe.ui.Icons;
import com.tienditadonpepe.ui.Insignias;
import com.tienditadonpepe.ui.Tablas;
import com.tienditadonpepe.ui.Tareas;
import com.tienditadonpepe.ui.Toast;
import com.tienditadonpepe.util.Dinero;
import com.tienditadonpepe.util.NegocioException;
import com.tienditadonpepe.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** RF05 (consultar existencias), RF06 (stock mínimo), RF08 (vencimiento) y mantenimiento del catálogo + ingresos (RF02). */
public class ProductosController {

    private enum FiltroSituacion {
        TODAS("Todas las situaciones", null), DISPONIBLE("Disponible", Producto.SituacionStock.DISPONIBLE),
        CRITICO("Stock crítico", Producto.SituacionStock.CRITICO), AGOTADO("Agotado", Producto.SituacionStock.AGOTADO),
        POR_VENCER("Próximos a vencer", null);

        final String texto;
        final Producto.SituacionStock valor;

        FiltroSituacion(String texto, Producto.SituacionStock valor) {
            this.texto = texto;
            this.valor = valor;
        }
    }

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<FiltroSituacion> cmbSituacion;
    @FXML private HBox accionesAdmin;
    @FXML private TableView<Producto> tabla;
    @FXML private Label lblResumen;

    private final ProductoService productos = new ProductoService();
    private final InventarioService inventario = new InventarioService();
    private final boolean admin = SesionUsuario.getInstance().getUsuario().esAdministrador();
    private List<Producto> todos = new ArrayList<>();

    @FXML
    private void initialize() {
        cmbSituacion.getItems().addAll(FiltroSituacion.values());
        cmbSituacion.setConverter(new StringConverter<>() {
            @Override
            public String toString(FiltroSituacion f) {
                return f == null ? "" : f.texto;
            }

            @Override
            public FiltroSituacion fromString(String s) {
                return null;
            }
        });
        cmbSituacion.getSelectionModel().selectFirst();
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        cmbSituacion.valueProperty().addListener((o, a, b) -> aplicarFiltro());

        Tablas.configurar(tabla, "No hay productos que coincidan con la búsqueda.", "caja");
        tabla.getColumns().add(Tablas.nodo("PRODUCTO", 300, p -> {
            Label n = new Label(p.getNombre() + " · " + p.getPresentacion());
            n.getStyleClass().add("negrita");
            Label m = new Label(p.getMarca() + "  ·  " + p.getProveedor_nombre());
            m.getStyleClass().addAll("texto-muted", "texto-peq");
            return new VBox(1, n, m);
        }));
        tabla.getColumns().add(Tablas.nodoCentro("STOCK", 70, p -> {
            Label l = new Label(String.valueOf(p.getStock_actual()));
            l.getStyleClass().add("negrita");
            l.setStyle("-fx-font-size: 15px;" + (p.getStock_actual() == 0 || p.verificarStockCritico() ? "-fx-text-fill: #C93B3B;" : p.esStockBajo() ? "-fx-text-fill: #B86E00;" : ""));
            return l;
        }));
        tabla.getColumns().add(Tablas.textoCentro("MÍNIMO", 80, p -> String.valueOf(p.getStock_minimo())));
        tabla.getColumns().add(Tablas.nodoCentro("SITUACIÓN", 120, Insignias::situacion));
        tabla.getColumns().add(Tablas.nodoCentro("VENCIMIENTO", 160, Insignias::vencimiento));
        tabla.getColumns().add(Tablas.textoDerecha("PRECIO", 80, p -> Dinero.soles(p.getPrecio_venta())));
        tabla.getColumns().add(Tablas.nodoCentro("ESTADO", 80, p -> Insignias.activo(p.getEstado())));
        if (admin) {
            tabla.getColumns().add(Tablas.nodo("ACCIONES", 120, p -> {
                HBox h = new HBox(2, icono("editar", "Editar producto", false, () -> dialogoProducto(p)),
                        icono("sube", "Registrar ingreso de latas", false, () -> dialogoIngreso(p)),
                        icono("papelera", "Eliminar producto", true, () -> eliminar(p)));
                h.setAlignment(Pos.CENTER_LEFT);
                return h;
            }));
            Button nuevo = Icons.en(new Button("Nuevo producto"), "mas", 16);
            nuevo.getStyleClass().add("btn-primary");
            nuevo.setOnAction(e -> dialogoProducto(null));
            Button ingreso = Icons.en(new Button("Registrar ingreso"), "sube", 16);
            ingreso.setOnAction(e -> dialogoIngreso(null));
            MenuItem miBaja = new MenuItem("Registrar baja (vencido, dañado, pérdida)");
            miBaja.setOnAction(e -> dialogoBaja());
            MenuItem miAjuste = new MenuItem("Ajuste por conteo físico");
            miAjuste.setOnAction(e -> dialogoAjuste());
            MenuButton mas = new MenuButton("Ajustar stock", Icons.crear("check", 16), miBaja, miAjuste);
            mas.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            accionesAdmin.getChildren().addAll(mas, ingreso, nuevo);
        }
        recargar();
    }

    private Button icono(String icono, String ayuda, boolean peligro, Runnable accion) {
        Button b = new Button(null, Icons.crear(icono, 16));
        b.getStyleClass().addAll("btn-icono-plano");
        if (peligro) b.getStyleClass().add("btn-icono-peligro");
        b.setTooltip(new Tooltip(ayuda));
        b.setOnAction(e -> accion.run());
        return b;
    }

    private void recargar() {
        Tareas.cargar(productos::listar, lista -> {
            todos = lista;
            aplicarFiltro();
        });
    }

    private void aplicarFiltro() {
        FiltroSituacion f = cmbSituacion.getValue() == null ? FiltroSituacion.TODAS : cmbSituacion.getValue();
        List<Producto> filtrados = ProductoService.filtrar(todos, txtBuscar.getText(), f.valor, f == FiltroSituacion.POR_VENCER);
        tabla.setItems(FXCollections.observableArrayList(filtrados));
        int unidades = filtrados.stream().mapToInt(Producto::getStock_actual).sum();
        lblResumen.setText(filtrados.size() + (filtrados.size() == 1 ? " producto" : " productos") + " · " + unidades + " latas en total");
    }

    private void despuesDeCambio(String mensaje) {
        Toast.ok(mensaje);
        recargar();
        MainController.refrescarAlertas();
    }

    // ---- Diálogos -----------------------------------------------------------------------------

    private List<Proveedor> proveedoresParaElegir(String idActual) {
        List<Proveedor> lista = new ArrayList<>(new ProveedorService().listarActivos());
        if (idActual != null && lista.stream().noneMatch(p -> p.getId_proveedor().equals(idActual))) {
            for (Proveedor p : new ProveedorService().listar()) {
                if (p.getId_proveedor().equals(idActual)) lista.add(p);
            }
        }
        return lista;
    }

    private void dialogoProducto(Producto existente) {
        boolean nuevo = existente == null;
        Formulario f = new Formulario(nuevo ? "Nuevo producto" : "Editar producto",
                nuevo ? "Registra una presentación de atún enlatado en el catálogo." : "El stock cambia solo con ingresos, ventas, bajas o ajustes.", 560);
        f.completo();
        TextField nombre = f.texto("Nombre del producto", nuevo ? "" : existente.getNombre(), "Ej. Atún en aceite");
        TextField marca = f.texto("Marca", nuevo ? "" : existente.getMarca(), "Ej. Costa Dorada");
        TextField presentacion = f.texto("Presentación", nuevo ? "" : existente.getPresentacion(), "Ej. Lata 170 g");
        TextField precio = f.texto("Precio de venta (S/, incluye IGV)", nuevo ? "" : Dinero.numero(existente.getPrecio_venta()), "0.00");
        List<Proveedor> provs = proveedoresParaElegir(nuevo ? null : existente.getId_proveedor());
        Proveedor actual = nuevo ? null : provs.stream().filter(p -> p.getId_proveedor().equals(existente.getId_proveedor())).findFirst().orElse(null);
        ComboBox<Proveedor> proveedor = f.combo("Proveedor", provs, actual, Proveedor::getRazon_social);
        TextField minimo = f.texto("Stock mínimo (alerta de reposición)", nuevo ? "20" : String.valueOf(existente.getStock_minimo()), "20");
        var vence = f.fecha(nuevo ? "Fecha de vencimiento del lote" : "Fecha de vencimiento", nuevo ? null : existente.getFecha_vencimiento(), null);
        TextField stockInicial = null;
        ComboBox<String> estado = null;
        if (nuevo) {
            stockInicial = f.texto("Stock inicial (latas)", "0", "0", "Se registra como un ingreso en el historial.");
        } else {
            estado = f.combo("Estado", List.of("Activo", "Inactivo"), existente.getEstado(), s -> s);
        }
        final TextField fStockInicial = stockInicial;
        final ComboBox<String> fEstado = estado;
        boolean ok = f.mostrar(nuevo ? "Registrar producto" : "Guardar cambios", () -> {
            double p = Validaciones.importe(precio.getText(), "precio de venta");
            int min = Validaciones.enteroNoNegativo(minimo.getText(), "stock mínimo");
            Proveedor pr = proveedor.getValue();
            if (pr == null) throw new NegocioException("Selecciona el proveedor.");
            if (nuevo) {
                int inicial = Validaciones.enteroNoNegativo(fStockInicial.getText(), "stock inicial");
                productos.registrar(nombre.getText(), marca.getText(), presentacion.getText(), p, inicial, min, vence.getValue(), pr.getId_proveedor());
            } else {
                productos.actualizar(existente.getId_producto(), nombre.getText(), marca.getText(), presentacion.getText(), p, min,
                        vence.getValue(), pr.getId_proveedor(), "Activo".equals(fEstado.getValue()));
            }
        });
        if (ok) despuesDeCambio(nuevo ? "Producto registrado correctamente." : "Producto actualizado correctamente.");
    }

    private void dialogoIngreso(Producto preseleccion) {
        List<Producto> activos = productos.listarActivos();
        if (activos.isEmpty()) {
            Dialogs.info("Sin productos", "Primero registra un producto para poder ingresar existencias.");
            return;
        }
        Formulario f = new Formulario("Registrar ingreso de latas", "Registra la mercadería recibida de un proveedor. El stock se actualiza automáticamente.", 540);
        f.completo();
        Producto sel = preseleccion == null ? null : activos.stream().filter(p -> p.getId_producto() == preseleccion.getId_producto()).findFirst().orElse(null);
        ComboBox<Producto> producto = f.combo("Producto", activos, sel, p -> p.getNombre() + " · " + p.getPresentacion() + " · " + p.getMarca());
        TextField cantidad = f.texto("Cantidad recibida (latas)", "", "Ej. 48");
        List<Proveedor> provs = new ProveedorService().listarActivos();
        ComboBox<Proveedor> proveedor = f.combo("Proveedor", provs, null, Proveedor::getRazon_social);
        var vence = f.fecha("Vencimiento del lote recibido", null, "Debe ser posterior a hoy.");
        TextField obs = f.texto("Observación (opcional)", "", "Ej. Factura F001-2345");
        Label resumen = f.nota("Selecciona el producto para ver el stock resultante.", "info");

        Runnable actualizar = () -> {
            Producto p = producto.getValue();
            if (p == null) return;
            proveedor.getItems().stream().filter(x -> x.getId_proveedor().equals(p.getId_proveedor())).findFirst().ifPresent(x -> {
                if (proveedor.getValue() == null) proveedor.setValue(x);
            });
            int c;
            try {
                c = Integer.parseInt(cantidad.getText().trim());
            } catch (NumberFormatException e) {
                c = 0;
            }
            resumen.setText("Stock actual: " + p.getStock_actual() + " latas" + (c > 0 ? "  →  quedará en " + (p.getStock_actual() + c) + " latas" : "")
                    + "  ·  Mínimo: " + p.getStock_minimo());
        };
        producto.valueProperty().addListener((o, a, b) -> actualizar.run());
        cantidad.textProperty().addListener((o, a, b) -> actualizar.run());
        actualizar.run();

        boolean ok = f.mostrar("Registrar ingreso", () -> {
            if (producto.getValue() == null) throw new NegocioException("Selecciona el producto.");
            int c = Validaciones.enteroPositivo(cantidad.getText(), "cantidad");
            if (proveedor.getValue() == null) throw new NegocioException("Selecciona el proveedor.");
            inventario.registrarIngreso(producto.getValue().getId_producto(), c, proveedor.getValue().getId_proveedor(), vence.getValue(), obs.getText());
        });
        if (ok) despuesDeCambio("Ingreso registrado: el stock se actualizó correctamente.");
    }

    private void dialogoBaja() {
        List<Producto> activos = productos.listar().stream().filter(p -> p.getStock_actual() > 0).collect(java.util.stream.Collectors.toList());
        if (activos.isEmpty()) {
            Dialogs.info("Sin existencias", "No hay productos con stock disponible para dar de baja.");
            return;
        }
        Formulario f = new Formulario("Registrar baja de inventario",
                "Úsalo para latas vencidas, dañadas, perdidas o de consumo interno (no para ventas).", 540);
        f.completo();
        ComboBox<Producto> producto = f.combo("Producto", activos, null, p -> p.getNombre() + " · " + p.getPresentacion() + " · " + p.getMarca()
                + "  (" + p.getStock_actual() + " disp.)");
        TextField cantidad = f.texto("Cantidad (latas)", "", "Ej. 2");
        ComboBox<String> motivo = f.combo("Motivo", List.of(InventarioService.MOTIVOS_BAJA), InventarioService.MOTIVOS_BAJA[0], s -> s);
        f.completo();
        TextField obs = f.texto("Observación (opcional)", "", "Detalle adicional");
        boolean ok = f.mostrar("Registrar baja", () -> {
            if (producto.getValue() == null) throw new NegocioException("Selecciona el producto.");
            int c = Validaciones.enteroPositivo(cantidad.getText(), "cantidad");
            inventario.registrarBaja(producto.getValue().getId_producto(), c, motivo.getValue(), obs.getText());
        });
        if (ok) despuesDeCambio("Baja registrada y stock actualizado.");
    }

    private void dialogoAjuste() {
        List<Producto> lista = productos.listarActivos();
        Formulario f = new Formulario("Ajuste por conteo físico",
                "Compara el stock registrado con lo contado en estantes y almacén; el sistema registra la diferencia como movimiento.", 540);
        f.completo();
        ComboBox<Producto> producto = f.combo("Producto", lista, null, p -> p.getNombre() + " · " + p.getPresentacion() + " · " + p.getMarca());
        TextField conteo = f.texto("Stock contado (latas)", "", "Cantidad física encontrada");
        TextField obs = f.texto("Observación (opcional)", "", "Ej. Inventario de fin de mes");
        Label resumen = f.nota("Selecciona el producto para ver el stock registrado.", "info");
        Runnable actualizar = () -> {
            Producto p = producto.getValue();
            if (p == null) return;
            String extra = "";
            try {
                int c = Integer.parseInt(conteo.getText().trim());
                int dif = c - p.getStock_actual();
                extra = dif == 0 ? "  ·  Sin diferencia" : "  ·  Diferencia: " + (dif > 0 ? "+" : "") + dif;
            } catch (NumberFormatException ignored) {
                // aún no hay conteo válido
            }
            resumen.setText("Stock registrado en el sistema: " + p.getStock_actual() + " latas" + extra);
        };
        producto.valueProperty().addListener((o, a, b) -> actualizar.run());
        conteo.textProperty().addListener((o, a, b) -> actualizar.run());
        boolean ok = f.mostrar("Aplicar ajuste", () -> {
            if (producto.getValue() == null) throw new NegocioException("Selecciona el producto.");
            int c = Validaciones.enteroNoNegativo(conteo.getText(), "stock contado");
            inventario.ajustarPorConteo(producto.getValue().getId_producto(), c, obs.getText());
        });
        if (ok) despuesDeCambio("Ajuste aplicado: el stock ahora coincide con el conteo físico.");
    }

    private void eliminar(Producto p) {
        if (!Dialogs.confirmar("Eliminar producto", "¿Eliminar «" + p.nombreCompleto() + " " + p.getMarca() + "»? Solo es posible si no tiene ventas ni movimientos; de lo contrario, inactívalo.",
                "Eliminar", true)) return;
        try {
            productos.eliminar(p.getId_producto());
            despuesDeCambio("Producto eliminado.");
        } catch (NegocioException e) {
            Dialogs.error("No se pudo eliminar", e.getMessage());
        }
    }
}
