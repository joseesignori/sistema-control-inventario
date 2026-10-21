package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.MovimientoInventario;

import java.time.LocalDate;
import java.util.List;

public interface IMovimientoDAO {
    void create(MovimientoInventario m);

    MovimientoInventario read(int idMovimiento);

    /** Movimientos filtrados (kardex). Cualquier filtro en null se ignora. Más recientes primero. */
    List<MovimientoInventario> buscar(LocalDate desde, LocalDate hasta, String tipo, Integer idProducto);

    List<MovimientoInventario> readRecientes(int limite);
}
