package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.Reportes.ResumenVentas;
import com.tienditadonpepe.model.Reportes.VentaDia;
import com.tienditadonpepe.model.Reportes.VentaPago;
import com.tienditadonpepe.model.Reportes.VentaProducto;
import com.tienditadonpepe.model.Reportes.VentaUsuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Consultas agregadas de solo lectura para reportes y panel principal. Solo cuentan ventas Emitidas. */
public class ReporteDAO extends DaoBase {

    private static void rango(PreparedStatement ps, LocalDate desde, LocalDate hasta) throws java.sql.SQLException {
        ps.setObject(1, desde.atStartOfDay());
        ps.setObject(2, hasta.plusDays(1).atStartOfDay());
    }

    public ResumenVentas resumen(LocalDate desde, LocalDate hasta) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) AS n, COALESCE(SUM(total),0) AS t FROM venta WHERE estado = 'Emitida' AND fecha_hora >= ? AND fecha_hora < ?")) {
                rango(ps, desde, hasta);
                int n;
                double t;
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    n = rs.getInt("n");
                    t = importe(rs, "t");
                }
                int unidades;
                try (PreparedStatement pu = c.prepareStatement(
                        "SELECT COALESCE(SUM(d.cantidad),0) FROM detalle_venta d JOIN venta v ON v.id_venta = d.id_venta " +
                                "WHERE v.estado = 'Emitida' AND v.fecha_hora >= ? AND v.fecha_hora < ?")) {
                    rango(pu, desde, hasta);
                    try (ResultSet rs = pu.executeQuery()) {
                        rs.next();
                        unidades = rs.getInt(1);
                    }
                }
                return new ResumenVentas(n, unidades, t, n == 0 ? 0 : t / n);
            }
        });
    }

    public List<VentaDia> ventasPorDia(LocalDate desde, LocalDate hasta) {
        return ejecutar(c -> {
            List<VentaDia> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT DATE(v.fecha_hora) AS dia, COUNT(DISTINCT v.id_venta) AS n, COALESCE(SUM(d.cantidad),0) AS u, " +
                            "COALESCE(SUM(d.subtotal),0) AS t FROM venta v JOIN detalle_venta d ON d.id_venta = v.id_venta " +
                            "WHERE v.estado = 'Emitida' AND v.fecha_hora >= ? AND v.fecha_hora < ? GROUP BY DATE(v.fecha_hora) ORDER BY dia")) {
                rango(ps, desde, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new VentaDia(fecha(rs, "dia"), rs.getInt("n"), rs.getInt("u"), importe(rs, "t")));
                    }
                }
            }
            return lista;
        });
    }

    public List<VentaProducto> ventasPorProducto(LocalDate desde, LocalDate hasta) {
        return ejecutar(c -> {
            List<VentaProducto> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT p.id_producto, CONCAT(p.nombre, ' · ', p.presentacion, ' · ', p.marca) AS nombre, SUM(d.cantidad) AS u, SUM(d.subtotal) AS t " +
                            "FROM detalle_venta d JOIN venta v ON v.id_venta = d.id_venta JOIN producto p ON p.id_producto = d.id_producto " +
                            "WHERE v.estado = 'Emitida' AND v.fecha_hora >= ? AND v.fecha_hora < ? " +
                            "GROUP BY p.id_producto, p.nombre, p.marca, p.presentacion ORDER BY u DESC")) {
                rango(ps, desde, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new VentaProducto(rs.getInt("id_producto"), rs.getString("nombre"), rs.getInt("u"), importe(rs, "t")));
                    }
                }
            }
            return lista;
        });
    }

    public List<VentaPago> ventasPorFormaPago(LocalDate desde, LocalDate hasta) {
        return ejecutar(c -> {
            List<VentaPago> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT forma_pago, COUNT(*) AS n, SUM(total) AS t FROM venta WHERE estado = 'Emitida' " +
                            "AND fecha_hora >= ? AND fecha_hora < ? GROUP BY forma_pago ORDER BY t DESC")) {
                rango(ps, desde, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(new VentaPago(rs.getString("forma_pago"), rs.getInt("n"), importe(rs, "t")));
                }
            }
            return lista;
        });
    }

    public List<VentaUsuario> ventasPorUsuario(LocalDate desde, LocalDate hasta) {
        return ejecutar(c -> {
            List<VentaUsuario> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT u.nombre_completo, COUNT(*) AS n, SUM(v.total) AS t FROM venta v JOIN usuario u ON u.id_usuario = v.id_usuario " +
                            "WHERE v.estado = 'Emitida' AND v.fecha_hora >= ? AND v.fecha_hora < ? GROUP BY u.id_usuario, u.nombre_completo ORDER BY t DESC")) {
                rango(ps, desde, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(new VentaUsuario(rs.getString("nombre_completo"), rs.getInt("n"), importe(rs, "t")));
                }
            }
            return lista;
        });
    }

    /** Cantidad de ventas anuladas en el rango (para el indicador de errores de registro). */
    public int contarAnuladas(LocalDate desde, LocalDate hasta) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM venta WHERE estado = 'Anulada' AND fecha_hora >= ? AND fecha_hora < ?")) {
                rango(ps, desde, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        });
    }
}
