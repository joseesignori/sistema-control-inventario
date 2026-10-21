package com.tienditadonpepe.model;

import java.time.LocalDate;

/** Estructuras de solo lectura que devuelven las consultas de reportes y del panel principal. */
public final class Reportes {

    private Reportes() {
    }

    public record ResumenVentas(int ventas, int unidades, double total, double ticketPromedio) {
    }

    public record VentaDia(LocalDate fecha, int ventas, int unidades, double total) {
    }

    public record VentaProducto(int idProducto, String producto, int unidades, double total) {
    }

    public record VentaPago(String formaPago, int ventas, double total) {
    }

    public record VentaUsuario(String usuario, int ventas, double total) {
    }
}
