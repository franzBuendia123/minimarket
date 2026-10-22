package com.lacanasta.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pruebas unitarias del modelo {@link CarritoItem}.
 */
class CarritoItemTest {

    @Test
    void debeDevolverPrecioEfectivoDelProducto() {
        Producto producto = new Producto();
        producto.setPrecioNormal(new BigDecimal("5.00"));
        producto.setPrecioDescuento(new BigDecimal("4.00"));
        CarritoItem item = new CarritoItem(producto, 2);

        assertEquals(0, item.getPrecio().compareTo(new BigDecimal("4.00")));
    }

    @Test
    void debeCalcularSubtotalComoPrecioPorCantidad() {
        Producto producto = new Producto();
        producto.setPrecioNormal(new BigDecimal("5.00"));
        producto.setPrecioDescuento(BigDecimal.ZERO);
        CarritoItem item = new CarritoItem(producto, 3);

        assertEquals(0, item.getSubtotal().compareTo(new BigDecimal("15.00")));
    }

    @Test
    void debeExponerIdYNombreDelProducto() {
        Producto producto = new Producto();
        producto.setIdProducto(7);
        producto.setNombre("Arroz");
        CarritoItem item = new CarritoItem(producto, 1);

        assertEquals(7, item.getIdProducto());
        assertEquals("Arroz", item.getNombre());
        assertSame(producto, item.getProducto());
    }

    @Test
    void debePermitirCambiarCantidadYRecalcularSubtotal() {
        Producto producto = new Producto();
        producto.setPrecioNormal(new BigDecimal("2.00"));
        CarritoItem item = new CarritoItem(producto, 1);

        item.setCantidad(5);

        assertEquals(5, item.getCantidad());
        assertEquals(0, item.getSubtotal().compareTo(new BigDecimal("10.00")));
    }
}
