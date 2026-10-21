package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.Proveedor;

import java.util.List;

public interface IProveedorDAO extends ICrudDAO<Proveedor, String> {
    String siguienteCodigo();

    List<Proveedor> readActivos();

    int contarProductos(String idProveedor);
}
