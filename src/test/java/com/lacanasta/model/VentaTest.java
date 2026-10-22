package com.lacanasta.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del modelo {@link Venta}.
 */
class VentaTest {

    @Test
    void debePermitirAsignarYLeerSusAtributos() {
        Venta venta = new Venta();
        venta.setIdVenta(10);
        venta.setIdCajero(2);
        venta.setIdCaja(3);
        venta.setTotal(new BigDecimal("9.30"));
        venta.setMetodoPago(MetodoPago.EFECTIVO);
        venta.setEstadoPago(EstadoPago.COMPLETADA);

        assertEquals(10, venta.getIdVenta());
        assertEquals(2, venta.getIdCajero());
        assertEquals(Integer.valueOf(3), venta.getIdCaja());
        assertEquals(0, venta.getTotal().compareTo(new BigDecimal("9.30")));
        assertEquals(MetodoPago.EFECTIVO, venta.getMetodoPago());
        assertEquals(EstadoPago.COMPLETADA, venta.getEstadoPago());
    }
}
