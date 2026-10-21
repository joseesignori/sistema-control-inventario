package com.tienditadonpepe.dao;

import java.util.List;

/** Contrato base del patrón DAO (apartado 2.1.4): crear, consultar, actualizar y eliminar. */
public interface ICrudDAO<T, ID> {
    void create(T entidad);

    T read(ID id);

    List<T> readAll();

    void update(T entidad);

    void delete(ID id);
}
