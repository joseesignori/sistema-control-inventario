package com.tienditadonpepe.service;

import com.tienditadonpepe.config.ConexionBD;
import com.tienditadonpepe.dao.IMovimientoDAO;
import com.tienditadonpepe.dao.IProductoDAO;
import com.tienditadonpepe.dao.IProveedorDAO;
import com.tienditadonpepe.dao.MovimientoDAO;
import com.tienditadonpepe.dao.ProductoDAO;
import com.tienditadonpepe.dao.ProveedorDAO;
import com.tienditadonpepe.model.MovimientoInventario;
import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Proveedor;
import com.tienditadonpepe.model.Usuario;
import com.tienditadonpepe.util.DatosException;
import com.tienditadonpepe.util.NegocioException;
import com.tienditadonpepe.util.Validaciones;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RF02 (ingreso), RF04 (actualización automática del stock), RF09 (historial de movimientos) y
 * alertas RF07/RF08. Toda operación que cambia el stock se ejecuta en una transacción con la fila
 * del producto bloqueada, y deja siempre un movimiento con el saldo resultante.
 */
public class InventarioService extends ServicioBase {

    public static final int CANTIDAD_MAXIMA = 100_000;

    public static final String[] MOTIVOS_BAJA = {"Producto vencido", "Lata dañada o abollada", "Pérdida o robo", "Consumo interno", "Otro"};

    private final IProductoDAO productoDAO = new ProductoDAO();
    private final IProveedorDAO proveedorDAO = new ProveedorDAO();
    private final IMovimientoDAO movimientoDAO = new MovimientoDAO();

    /** Alertas de stock crítico y vencimiento sobre los productos activos. */
    public static final class Alertas {
        public final List<Producto> stockCritico = new ArrayList<>();
        public final List<Producto> porVencer = new ArrayList<>();

        public boolean hayAlertas() {
            return !stockCritico.isEmpty() || !porVencer.isEmpty();
        }
    }

    /**
     * RF02: registra el ingreso de latas. El lote que entra puede traer su fecha de vencimiento; como el
     * producto guarda una sola fecha, se conserva la más próxima mientras haya existencias vigentes.
     */
    public MovimientoInventario registrarIngreso(int idProducto, int cantidad, String idProveedor,
                                                 LocalDate vencimientoLote, String observacion) {
        Usuario u = sesion.exigirAdministrador();
        validarCantidad(cantidad);
        if (idProveedor == null || idProveedor.isBlank()) throw new NegocioException("Selecciona el proveedor que entrega el lote.");
        if (vencimientoLote == null) throw new NegocioException("Indica la fecha de vencimiento del lote recibido.");
        if (!vencimientoLote.isAfter(LocalDate.now())) {
            throw new NegocioException("El lote recibido ya está vencido o vence hoy; no se puede ingresar al inventario.");
        }
        String obs = Validaciones.opcional(observacion, "la observación", 100);
        try {
            return ConexionBD.getInstance().enTransaccion(() -> {
                Producto p = productoDAO.readParaActualizar(idProducto);
                if (p == null) throw new NegocioException("El producto ya no existe.");
                if (!p.estaActivo()) throw new NegocioException("El producto está inactivo; actívalo antes de registrar ingresos.");
                Proveedor pr = proveedorDAO.read(idProveedor);
                if (pr == null) throw new NegocioException("El proveedor seleccionado no existe.");
                if (!pr.estaActivo()) throw new NegocioException("El proveedor seleccionado está inactivo.");
                long nuevoStock = (long) p.getStock_actual() + cantidad;
                if (nuevoStock > CANTIDAD_MAXIMA) {
                    throw new NegocioException("El stock resultante (" + nuevoStock + ") supera el máximo permitido de " + CANTIDAD_MAXIMA + " unidades.");
                }
                LocalDate vencimiento = vencimientoLote;
                boolean hayExistenciaVigente = p.getStock_actual() > 0 && p.getFecha_vencimiento() != null && !p.estaVencido();
                if (hayExistenciaVigente && p.getFecha_vencimiento().isBefore(vencimientoLote)) {
                    vencimiento = p.getFecha_vencimiento();
                }
                productoDAO.updateStockYVencimiento(idProducto, (int) nuevoStock, vencimiento);

                MovimientoInventario m = new MovimientoInventario();
                m.setFecha_hora(LocalDateTime.now());
                m.setTipo_movimiento(MovimientoInventario.TIPO_INGRESO);
                m.setId_producto(idProducto);
                m.setCantidad(cantidad);
                m.setSaldo((int) nuevoStock);
                m.setDetalle("Ingreso de lote (vence " + com.tienditadonpepe.util.Fechas.fecha(vencimientoLote) + ")"
                        + (obs == null ? "" : " · " + obs));
                m.setId_usuario(u.getId_usuario());
                m.setId_proveedor(idProveedor);
                movimientoDAO.create(m);
                return m;
            });
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    /** Salida que no es venta: merma, vencimiento, daño, etc. */
    public MovimientoInventario registrarBaja(int idProducto, int cantidad, String motivo, String observacion) {
        Usuario u = sesion.exigirAdministrador();
        validarCantidad(cantidad);
        String mot = Validaciones.requerido(motivo, "el motivo de la baja", 60);
        String obs = Validaciones.opcional(observacion, "la observación", 80);
        try {
            return ConexionBD.getInstance().enTransaccion(() -> {
                Producto p = productoDAO.readParaActualizar(idProducto);
                if (p == null) throw new NegocioException("El producto ya no existe.");
                if (cantidad > p.getStock_actual()) {
                    throw new NegocioException("Stock insuficiente: solo hay " + p.getStock_actual() + " unidades disponibles de " + p.nombreCompleto() + ".");
                }
                int nuevoStock = p.getStock_actual() - cantidad;
                productoDAO.updateStock(idProducto, nuevoStock);
                MovimientoInventario m = salida(idProducto, cantidad, nuevoStock, "Baja: " + mot + (obs == null ? "" : " · " + obs), u, null);
                movimientoDAO.create(m);
                return m;
            });
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    /** Ajusta el stock registrado al conteo físico, generando el movimiento por la diferencia. */
    public MovimientoInventario ajustarPorConteo(int idProducto, int stockFisico, String observacion) {
        Usuario u = sesion.exigirAdministrador();
        if (stockFisico < 0 || stockFisico > CANTIDAD_MAXIMA) throw new NegocioException("El conteo físico debe estar entre 0 y " + CANTIDAD_MAXIMA + ".");
        String obs = Validaciones.opcional(observacion, "la observación", 80);
        try {
            return ConexionBD.getInstance().enTransaccion(() -> {
                Producto p = productoDAO.readParaActualizar(idProducto);
                if (p == null) throw new NegocioException("El producto ya no existe.");
                int dif = stockFisico - p.getStock_actual();
                if (dif == 0) throw new NegocioException("El conteo físico coincide con el stock registrado; no hay nada que ajustar.");
                productoDAO.updateStock(idProducto, stockFisico);
                String detalle = "Ajuste por conteo físico (" + p.getStock_actual() + " → " + stockFisico + ")" + (obs == null ? "" : " · " + obs);
                MovimientoInventario m;
                if (dif > 0) {
                    m = new MovimientoInventario();
                    m.setFecha_hora(LocalDateTime.now());
                    m.setTipo_movimiento(MovimientoInventario.TIPO_INGRESO);
                    m.setId_producto(idProducto);
                    m.setCantidad(dif);
                    m.setSaldo(stockFisico);
                    m.setDetalle(detalle);
                    m.setId_usuario(u.getId_usuario());
                } else {
                    m = salida(idProducto, -dif, stockFisico, detalle, u, null);
                }
                movimientoDAO.create(m);
                return m;
            });
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    /** RF09: historial de entradas y salidas con filtros (fechas inclusivas). */
    public List<MovimientoInventario> obtenerMovimientos(LocalDate desde, LocalDate hasta, String tipo, Integer idProducto) {
        sesion.exigir();
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new NegocioException("La fecha «desde» no puede ser posterior a la fecha «hasta».");
        }
        try {
            return movimientoDAO.buscar(desde, hasta, tipo, idProducto);
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    public List<MovimientoInventario> movimientosRecientes(int limite) {
        sesion.exigir();
        try {
            return movimientoDAO.readRecientes(limite);
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    /** RF07 y RF08: productos activos con stock crítico o próximos a vencer / vencidos. */
    public Alertas obtenerAlertas() {
        sesion.exigir();
        Alertas a = new Alertas();
        try {
            for (Producto p : productoDAO.readActivos()) {
                if (p.verificarStockCritico()) a.stockCritico.add(p);
                if (p.verificarVencimiento() && p.getStock_actual() > 0) a.porVencer.add(p);
            }
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
        a.porVencer.sort((x, y) -> x.getFecha_vencimiento().compareTo(y.getFecha_vencimiento()));
        a.stockCritico.sort((x, y) -> Integer.compare(x.getStock_actual(), y.getStock_actual()));
        return a;
    }

    // ---- Apoyo (también lo usa VentaService dentro de su transacción) -----------------------

    static MovimientoInventario salida(int idProducto, int cantidad, int saldo, String detalle, Usuario u, Integer idVenta) {
        MovimientoInventario m = new MovimientoInventario();
        m.setFecha_hora(LocalDateTime.now());
        m.setTipo_movimiento(MovimientoInventario.TIPO_SALIDA);
        m.setId_producto(idProducto);
        m.setCantidad(cantidad);
        m.setSaldo(saldo);
        m.setDetalle(detalle);
        m.setId_usuario(u.getId_usuario());
        m.setId_venta(idVenta);
        return m;
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) throw new NegocioException("La cantidad debe ser un número entero mayor que cero.");
        if (cantidad > CANTIDAD_MAXIMA) throw new NegocioException("La cantidad no puede superar " + CANTIDAD_MAXIMA + " unidades por operación.");
    }
}
