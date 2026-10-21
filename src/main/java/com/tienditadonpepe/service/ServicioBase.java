package com.tienditadonpepe.service;

import com.tienditadonpepe.util.DatosException;
import com.tienditadonpepe.util.NegocioException;

/** Comportamiento común de los servicios: traducción de errores de datos a mensajes de negocio. */
abstract class ServicioBase {

    protected final SesionUsuario sesion = SesionUsuario.getInstance();

    /**
     * Convierte un error de datos en un mensaje comprensible. Los textos por tipo se pasan desde cada servicio;
     * si no se indica uno se usa un mensaje genérico.
     */
    protected NegocioException traducir(DatosException e, String duplicado, String referenciado) {
        switch (e.getTipo()) {
            case DUPLICADO:
                return new NegocioException(duplicado != null ? duplicado : "Ya existe un registro con esos datos.");
            case REFERENCIADO:
                return new NegocioException(referenciado != null ? referenciado
                        : "No se puede eliminar porque tiene registros asociados.");
            case SIN_REFERENCIA:
                return new NegocioException("Uno de los datos relacionados ya no existe. Actualiza la pantalla e inténtalo otra vez.");
            case RESTRICCION:
                return new NegocioException("Los datos ingresados no cumplen las reglas del sistema. Revísalos e inténtalo otra vez.");
            case CONEXION:
                return new NegocioException("Se perdió la conexión con la base de datos. Verifica que MySQL esté encendido.");
            default:
                return new NegocioException("No se pudo completar la operación: " + e.getMessage());
        }
    }
}
