package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.Producto;

import java.time.LocalDate;
import java.util.List;

public interface IProductoDAO extends ICrudDAO<Producto, Integer> {
    List<Producto> readActivos();

    /** Lee el producto bloqueando la fila (SELECT ... FOR UPDATE) dentro de una transacción. */
    Producto readParaActualizar(int idProducto);

    void updateStock(int idProducto, int nuevoStock);

    void updateStockYVencimiento(int idProducto, int nuevoStock, LocalDate fechaVencimiento);

    void updateStockMinimo(int idProducto, int stockMinimo);
}
