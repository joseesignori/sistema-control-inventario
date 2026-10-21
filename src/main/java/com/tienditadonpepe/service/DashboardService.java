package com.tienditadonpepe.service;

import com.tienditadonpepe.dao.IProductoDAO;
import com.tienditadonpepe.dao.IMovimientoDAO;
import com.tienditadonpepe.dao.MovimientoDAO;
import com.tienditadonpepe.dao.ProductoDAO;
import com.tienditadonpepe.dao.ReporteDAO;
import com.tienditadonpepe.model.MovimientoInventario;
import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.model.Reportes.ResumenVentas;
import com.tienditadonpepe.model.Reportes.VentaDia;
import com.tienditadonpepe.util.DatosException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Datos del panel principal: indicadores del día, alertas, gráficos y últimos movimientos. */
public class DashboardService extends ServicioBase {

    /** Instantánea de todo lo que muestra el panel. */
    public static final class Panel {
        public int stockTotal;
        public int productosActivos;
        public final List<Producto> productos = new ArrayList<>();
        public final List<Producto> stockCritico = new ArrayList<>();
        public final List<Producto> porVencer = new ArrayList<>();
        public int disponibles;
        public int criticos;
        public int agotados;
        public ResumenVentas ventasMes;
        public ResumenVentas ventasHoy;
        /** Ventas de los últimos 14 días, incluidos los días sin ventas (total 0). */
        public final List<VentaDia> ventasPorDia = new ArrayList<>();
        public final List<MovimientoInventario> ultimosMovimientos = new ArrayList<>();
    }

    private static final int DIAS_GRAFICO = 14;

    private final IProductoDAO productoDAO = new ProductoDAO();
    private final IMovimientoDAO movimientoDAO = new MovimientoDAO();
    private final ReporteDAO reporteDAO = new ReporteDAO();

    public Panel cargar() {
        sesion.exigir();
        Panel p = new Panel();
        try {
            for (Producto prod : productoDAO.readActivos()) {
                p.productos.add(prod);
                p.productosActivos++;
                p.stockTotal += prod.getStock_actual();
                switch (prod.situacionStock()) {
                    case AGOTADO:
                        p.agotados++;
                        break;
                    case CRITICO:
                        p.criticos++;
                        break;
                    default:
                        p.disponibles++;
                }
                if (prod.verificarStockCritico()) p.stockCritico.add(prod);
                if (prod.verificarVencimiento() && prod.getStock_actual() > 0) p.porVencer.add(prod);
            }
            p.porVencer.sort((a, b) -> a.getFecha_vencimiento().compareTo(b.getFecha_vencimiento()));
            LocalDate hoy = LocalDate.now();
            p.ventasMes = reporteDAO.resumen(hoy.withDayOfMonth(1), hoy);
            p.ventasHoy = reporteDAO.resumen(hoy, hoy);

            LocalDate desde = hoy.minusDays(DIAS_GRAFICO - 1L);
            Map<LocalDate, VentaDia> porFecha = new HashMap<>();
            for (VentaDia d : reporteDAO.ventasPorDia(desde, hoy)) porFecha.put(d.fecha(), d);
            for (int i = 0; i < DIAS_GRAFICO; i++) {
                LocalDate f = desde.plusDays(i);
                p.ventasPorDia.add(porFecha.getOrDefault(f, new VentaDia(f, 0, 0, 0)));
            }
            p.ultimosMovimientos.addAll(movimientoDAO.readRecientes(8));
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
        return p;
    }
}
