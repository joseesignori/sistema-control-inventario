package com.tienditadonpepe.service;

import com.tienditadonpepe.config.ConexionBD;
import com.tienditadonpepe.dao.IProductoDAO;
import com.tienditadonpepe.dao.IProveedorDAO;
import com.tienditadonpepe.dao.MovimientoDAO;
import com.tienditadonpepe.dao.IMovimientoDAO;
import com.tienditadonpepe.dao.ProductoDAO;
import com.tienditadonpepe.dao.ProveedorDAO;
import com.tienditadonpepe.model.MovimientoInventario;
import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Proveedor;
import com.tienditadonpepe.model.Usuario;
import com.tienditadonpepe.util.DatosException;
import com.tienditadonpepe.util.Dinero;
import com.tienditadonpepe.util.NegocioException;
import com.tienditadonpepe.util.Validaciones;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** Catálogo de atún enlatado: RF05 (consultar), RF06 (stock mínimo), RF08 (vencimiento). */
public class ProductoService extends ServicioBase {

    private final IProductoDAO dao = new ProductoDAO();
    private final IProveedorDAO proveedorDAO = new ProveedorDAO();
    private final IMovimientoDAO movimientoDAO = new MovimientoDAO();

    public List<Producto> listar() {
        sesion.exigir();
        try {
            return dao.readAll();
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    public List<Producto> listarActivos() {
        sesion.exigir();
        try {
            return dao.readActivos();
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    public Producto obtener(int id) {
        sesion.exigir();
        try {
            return dao.read(id);
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    /** RF05: consulta con filtros por texto, situación del stock y vencimiento. */
    public List<Producto> consultar(String texto, Producto.SituacionStock situacion, boolean soloPorVencer) {
        return filtrar(listar(), texto, situacion, soloPorVencer);
    }

    /** Filtra una lista ya cargada (la pantalla de consulta filtra al instante mientras el usuario escribe). */
    public static List<Producto> filtrar(List<Producto> productos, String texto, Producto.SituacionStock situacion, boolean soloPorVencer) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        return productos.stream()
                .filter(p -> t.isEmpty() || p.getNombre().toLowerCase().contains(t) || p.getMarca().toLowerCase().contains(t)
                        || p.getPresentacion().toLowerCase().contains(t))
                .filter(p -> situacion == null || p.situacionStock() == situacion)
                .filter(p -> !soloPorVencer || p.verificarVencimiento())
                .collect(Collectors.toList());
    }

    /** Registra un producto nuevo; si hay stock inicial lo asienta como un ingreso en el historial. */
    public Producto registrar(String nombre, String marca, String presentacion, double precioVenta, int stockInicial,
                              int stockMinimo, LocalDate vencimiento, String idProveedor) {
        Usuario u = sesion.exigirAdministrador();
        Producto p = armar(nombre, marca, presentacion, precioVenta, stockMinimo, vencimiento, idProveedor);
        if (stockInicial < 0) throw new NegocioException("El stock inicial no puede ser negativo.");
        if (stockInicial > 100_000) throw new NegocioException("El stock inicial es demasiado alto.");
        if (!vencimiento.isAfter(LocalDate.now())) {
            throw new NegocioException("La fecha de vencimiento debe ser posterior a hoy.");
        }
        p.setStock_actual(stockInicial);
        p.setEstado(Producto.ESTADO_ACTIVO);
        try {
            return ConexionBD.getInstance().enTransaccion(() -> {
                validarProveedorActivo(idProveedor);
                dao.create(p);
                if (stockInicial > 0) {
                    MovimientoInventario m = new MovimientoInventario();
                    m.setFecha_hora(LocalDateTime.now());
                    m.setTipo_movimiento(MovimientoInventario.TIPO_INGRESO);
                    m.setId_producto(p.getId_producto());
                    m.setCantidad(stockInicial);
                    m.setSaldo(stockInicial);
                    m.setDetalle("Stock inicial del producto");
                    m.setId_usuario(u.getId_usuario());
                    m.setId_proveedor(idProveedor);
                    movimientoDAO.create(m);
                }
                return p;
            });
        } catch (DatosException e) {
            throw traducir(e, "Ya existe un producto con el mismo nombre, marca y presentación.", null);
        }
    }

    /** Actualiza los datos del producto (el stock solo cambia con movimientos de inventario). */
    public void actualizar(int idProducto, String nombre, String marca, String presentacion, double precioVenta,
                           int stockMinimo, LocalDate vencimiento, String idProveedor, boolean activo) {
        sesion.exigirAdministrador();
        Producto datos = armar(nombre, marca, presentacion, precioVenta, stockMinimo, vencimiento, idProveedor);
        try {
            ConexionBD.getInstance().enTransaccion(() -> {
                Producto p = dao.readParaActualizar(idProducto);
                if (p == null) throw new NegocioException("El producto ya no existe.");
                if (!idProveedor.equals(p.getId_proveedor())) validarProveedorActivo(idProveedor);
                p.setNombre(datos.getNombre());
                p.setMarca(datos.getMarca());
                p.setPresentacion(datos.getPresentacion());
                p.setPrecio_venta(datos.getPrecio_venta());
                p.setStock_minimo(datos.getStock_minimo());
                p.setFecha_vencimiento(vencimiento);
                p.setId_proveedor(idProveedor);
                p.setEstado(activo ? Producto.ESTADO_ACTIVO : Producto.ESTADO_INACTIVO);
                dao.update(p);
                return null;
            });
        } catch (DatosException e) {
            throw traducir(e, "Ya existe otro producto con el mismo nombre, marca y presentación.", null);
        }
    }

    /** RF06: configura el stock mínimo a partir del cual se alerta. */
    public void configurarStockMinimo(int idProducto, int stockMinimo) {
        sesion.exigirAdministrador();
        if (stockMinimo < 0 || stockMinimo > 100_000) throw new NegocioException("El stock mínimo debe estar entre 0 y 100 000 unidades.");
        try {
            if (dao.read(idProducto) == null) throw new NegocioException("El producto ya no existe.");
            dao.updateStockMinimo(idProducto, stockMinimo);
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    public void eliminar(int idProducto) {
        sesion.exigirAdministrador();
        try {
            dao.delete(idProducto);
        } catch (DatosException e) {
            throw traducir(e, null, "No se puede eliminar el producto porque tiene ventas o movimientos registrados. Puedes inactivarlo.");
        }
    }

    private Producto armar(String nombre, String marca, String presentacion, double precio, int stockMinimo,
                           LocalDate vencimiento, String idProveedor) {
        Producto p = new Producto();
        p.setNombre(Validaciones.requerido(nombre, "el nombre del producto", 100));
        p.setMarca(Validaciones.requerido(marca, "la marca", 60));
        p.setPresentacion(Validaciones.requerido(presentacion, "la presentación", 50));
        if (Double.isNaN(precio) || precio <= 0) throw new NegocioException("El precio de venta debe ser mayor que cero.");
        if (precio > 99_999) throw new NegocioException("El precio de venta es demasiado alto.");
        p.setPrecio_venta(Dinero.redondear(precio));
        if (stockMinimo < 0 || stockMinimo > 100_000) throw new NegocioException("El stock mínimo debe estar entre 0 y 100 000 unidades.");
        p.setStock_minimo(stockMinimo);
        if (vencimiento == null) throw new NegocioException("Indica la fecha de vencimiento.");
        p.setFecha_vencimiento(vencimiento);
        if (idProveedor == null || idProveedor.isBlank()) throw new NegocioException("Selecciona el proveedor.");
        p.setId_proveedor(idProveedor);
        return p;
    }

    private void validarProveedorActivo(String idProveedor) {
        Proveedor pr = proveedorDAO.read(idProveedor);
        if (pr == null) throw new NegocioException("El proveedor seleccionado no existe.");
        if (!pr.estaActivo()) throw new NegocioException("El proveedor seleccionado está inactivo.");
    }
}
