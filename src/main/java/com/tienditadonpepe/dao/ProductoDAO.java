package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.Producto;
import com.tienditadonpepe.util.Dinero;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO extends DaoBase implements IProductoDAO {

    private static final String SELECT =
            "SELECT p.*, pr.razon_social AS proveedor_nombre FROM producto p JOIN proveedor pr ON pr.id_proveedor = p.id_proveedor ";

    private static Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId_producto(rs.getInt("id_producto"));
        p.setNombre(rs.getString("nombre"));
        p.setMarca(rs.getString("marca"));
        p.setPresentacion(rs.getString("presentacion"));
        p.setPrecio_venta(importe(rs, "precio_venta"));
        p.setStock_actual(rs.getInt("stock_actual"));
        p.setStock_minimo(rs.getInt("stock_minimo"));
        p.setFecha_vencimiento(fecha(rs, "fecha_vencimiento"));
        p.setEstado(rs.getString("estado"));
        p.setId_proveedor(rs.getString("id_proveedor"));
        p.setProveedor_nombre(rs.getString("proveedor_nombre"));
        return p;
    }

    @Override
    public void create(Producto p) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO producto (nombre, marca, presentacion, precio_venta, stock_actual, stock_minimo, fecha_vencimiento, estado, id_proveedor) " +
                            "VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getMarca());
                ps.setString(3, p.getPresentacion());
                ps.setBigDecimal(4, Dinero.aBigDecimal(p.getPrecio_venta()));
                ps.setInt(5, p.getStock_actual());
                ps.setInt(6, p.getStock_minimo());
                ps.setObject(7, p.getFecha_vencimiento());
                ps.setString(8, p.getEstado());
                ps.setString(9, p.getId_proveedor());
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) p.setId_producto(k.getInt(1));
                }
            }
            return null;
        });
    }

    @Override
    public Producto read(Integer id) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.id_producto = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
    }

    @Override
    public Producto readParaActualizar(int idProducto) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.id_producto = ? FOR UPDATE")) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
    }

    @Override
    public List<Producto> readAll() {
        return ejecutar(c -> {
            List<Producto> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY p.nombre, p.presentacion");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        });
    }

    @Override
    public List<Producto> readActivos() {
        return ejecutar(c -> {
            List<Producto> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.estado = 'Activo' ORDER BY p.nombre, p.presentacion");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        });
    }

    @Override
    public void update(Producto p) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE producto SET nombre = ?, marca = ?, presentacion = ?, precio_venta = ?, stock_minimo = ?, " +
                            "fecha_vencimiento = ?, estado = ?, id_proveedor = ? WHERE id_producto = ?")) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getMarca());
                ps.setString(3, p.getPresentacion());
                ps.setBigDecimal(4, Dinero.aBigDecimal(p.getPrecio_venta()));
                ps.setInt(5, p.getStock_minimo());
                ps.setObject(6, p.getFecha_vencimiento());
                ps.setString(7, p.getEstado());
                ps.setString(8, p.getId_proveedor());
                ps.setInt(9, p.getId_producto());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void updateStock(int idProducto, int nuevoStock) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE producto SET stock_actual = ? WHERE id_producto = ?")) {
                ps.setInt(1, nuevoStock);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void updateStockYVencimiento(int idProducto, int nuevoStock, LocalDate fechaVencimiento) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE producto SET stock_actual = ?, fecha_vencimiento = ? WHERE id_producto = ?")) {
                ps.setInt(1, nuevoStock);
                ps.setObject(2, fechaVencimiento);
                ps.setInt(3, idProducto);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void updateStockMinimo(int idProducto, int stockMinimo) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE producto SET stock_minimo = ? WHERE id_producto = ?")) {
                ps.setInt(1, stockMinimo);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void delete(Integer id) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM producto WHERE id_producto = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            return null;
        });
    }
}
