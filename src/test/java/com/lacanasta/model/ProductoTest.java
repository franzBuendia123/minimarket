package com.lacanasta.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del modelo {@link Producto}.
 */
class ProductoTest {

    @Test
    void debeUsarPrecioDescuentoCuandoEsMayorQueCero() {
        Producto producto = new Producto();
        producto.setPrecioNormal(new BigDecimal("4.50"));
        producto.setPrecioDescuento(new BigDecimal("4.00"));

        assertEquals(0, producto.getPrecioVenta().compareTo(new BigDecimal("4.00")));
    }

    @Test
    void debeUsarPrecioNormalCuandoDescuentoEsCero() {
        Producto producto = new Producto();
        producto.setPrecioNormal(new BigDecimal("4.50"));
        producto.setPrecioDescuento(BigDecimal.ZERO);

        assertEquals(0, producto.getPrecioVenta().compareTo(new BigDecimal("4.50")));
    }

    @Test
    void debeUsarPrecioNormalCuandoDescuentoEsNulo() {
        Producto producto = new Producto();
        producto.setPrecioNormal(new BigDecimal("4.50"));
        producto.setPrecioDescuento(null);

        assertEquals(0, producto.getPrecioVenta().compareTo(new BigDecimal("4.50")));
    }

    @Test
    void debePermitirAsignarYLeerSusAtributos() {
        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setCodigoBarras("7891000012345");
        producto.setNombre("Coca-Cola 1.5L");
        producto.setStock(50);
        producto.setEstado(true);
        producto.setIdCategoria(2);

        assertEquals(1, producto.getIdProducto());
        assertEquals("7891000012345", producto.getCodigoBarras());
        assertEquals("Coca-Cola 1.5L", producto.getNombre());
        assertEquals(50, producto.getStock());
        assertTrue(producto.isEstado());
        assertEquals(Integer.valueOf(2), producto.getIdCategoria());
    }
}
