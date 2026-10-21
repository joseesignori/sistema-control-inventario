package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.MovimientoInventario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO extends DaoBase implements IMovimientoDAO {

    private static final String SELECT =
            "SELECT m.*, CONCAT(p.nombre, ' · ', p.presentacion, ' · ', p.marca) AS producto_nombre, u.nombre_completo AS usuario_nombre, " +
            "pr.razon_social AS proveedor_nombre FROM movimiento_inventario m " +
            "JOIN producto p ON p.id_producto = m.id_producto JOIN usuario u ON u.id_usuario = m.id_usuario " +
            "LEFT JOIN proveedor pr ON pr.id_proveedor = m.id_proveedor ";

    private static MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setId_movimiento(rs.getInt("id_movimiento"));
        m.setFecha_hora(fechaHora(rs, "fecha_hora"));
        m.setTipo_movimiento(rs.getString("tipo_movimiento"));
        m.setId_producto(rs.getInt("id_producto"));
        m.setCantidad(rs.getInt("cantidad"));
        m.setSaldo(rs.getInt("saldo"));
        m.setDetalle(rs.getString("detalle"));
        m.setId_usuario(rs.getInt("id_usuario"));
        m.setId_venta(entero(rs, "id_venta"));
        m.setId_proveedor(rs.getString("id_proveedor"));
        m.setProducto_nombre(rs.getString("producto_nombre"));
        m.setUsuario_nombre(rs.getString("usuario_nombre"));
        m.setProveedor_nombre(rs.getString("proveedor_nombre"));
        return m;
    }

    @Override
    public void create(MovimientoInventario m) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO movimiento_inventario (fecha_hora, tipo_movimiento, id_producto, cantidad, saldo, detalle, id_usuario, id_venta, id_proveedor) " +
                            "VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, m.getFecha_hora());
                ps.setString(2, m.getTipo_movimiento());
                ps.setInt(3, m.getId_producto());
                ps.setInt(4, m.getCantidad());
                ps.setInt(5, m.getSaldo());
                setTexto(ps, 6, m.getDetalle());
                ps.setInt(7, m.getId_usuario());
                setEntero(ps, 8, m.getId_venta());
                setTexto(ps, 9, m.getId_proveedor());
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) m.setId_movimiento(k.getInt(1));
                }
            }
            return null;
        });
    }

    @Override
    public MovimientoInventario read(int id) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE m.id_movimiento = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
    }

    @Override
    public List<MovimientoInventario> buscar(LocalDate desde, LocalDate hasta, String tipo, Integer idProducto) {
        return ejecutar(c -> {
            StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
            List<Object> params = new ArrayList<>();
            if (desde != null) {
                sql.append("AND m.fecha_hora >= ? ");
                params.add(desde.atStartOfDay());
            }
            if (hasta != null) {
                sql.append("AND m.fecha_hora < ? ");
                params.add(hasta.plusDays(1).atStartOfDay());
            }
            if (tipo != null && !tipo.isBlank()) {
                sql.append("AND m.tipo_movimiento = ? ");
                params.add(tipo);
            }
            if (idProducto != null) {
                sql.append("AND m.id_producto = ? ");
                params.add(idProducto);
            }
            sql.append("ORDER BY m.fecha_hora DESC, m.id_movimiento DESC");
            List<MovimientoInventario> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
            return lista;
        });
    }

    @Override
    public List<MovimientoInventario> readRecientes(int limite) {
        return ejecutar(c -> {
            List<MovimientoInventario> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY m.fecha_hora DESC, m.id_movimiento DESC LIMIT ?")) {
                ps.setInt(1, limite);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
            return lista;
        });
    }
}
