package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.Proveedor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO extends DaoBase implements IProveedorDAO {

    private static Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId_proveedor(rs.getString("id_proveedor"));
        p.setRazon_social(rs.getString("razon_social"));
        p.setRuc(rs.getString("ruc"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setDireccion(rs.getString("direccion"));
        p.setEstado(rs.getString("estado"));
        return p;
    }

    @Override
    public String siguienteCodigo() {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COALESCE(MAX(CAST(SUBSTRING(id_proveedor, 2) AS UNSIGNED)), 0) FROM proveedor");
                 ResultSet rs = ps.executeQuery()) {
                int n = rs.next() ? rs.getInt(1) : 0;
                return String.format("P%03d", n + 1);
            }
        });
    }

    @Override
    public void create(Proveedor p) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO proveedor (id_proveedor, razon_social, ruc, telefono, correo, direccion, estado) VALUES (?,?,?,?,?,?,?)")) {
                ps.setString(1, p.getId_proveedor());
                ps.setString(2, p.getRazon_social());
                ps.setString(3, p.getRuc());
                setTexto(ps, 4, p.getTelefono());
                setTexto(ps, 5, p.getCorreo());
                setTexto(ps, 6, p.getDireccion());
                ps.setString(7, p.getEstado());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public Proveedor read(String id) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM proveedor WHERE id_proveedor = ?")) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
    }

    @Override
    public List<Proveedor> readAll() {
        return ejecutar(c -> {
            List<Proveedor> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM proveedor ORDER BY id_proveedor");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        });
    }

    @Override
    public List<Proveedor> readActivos() {
        return ejecutar(c -> {
            List<Proveedor> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM proveedor WHERE estado = 'Activo' ORDER BY razon_social");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        });
    }

    @Override
    public void update(Proveedor p) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE proveedor SET razon_social = ?, ruc = ?, telefono = ?, correo = ?, direccion = ?, estado = ? WHERE id_proveedor = ?")) {
                ps.setString(1, p.getRazon_social());
                ps.setString(2, p.getRuc());
                setTexto(ps, 3, p.getTelefono());
                setTexto(ps, 4, p.getCorreo());
                setTexto(ps, 5, p.getDireccion());
                ps.setString(6, p.getEstado());
                ps.setString(7, p.getId_proveedor());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void delete(String id) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM proveedor WHERE id_proveedor = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public int contarProductos(String idProveedor) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM producto WHERE id_proveedor = ? AND estado = 'Activo'")) {
                ps.setString(1, idProveedor);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        });
    }
}
