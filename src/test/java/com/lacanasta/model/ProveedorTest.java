package com.lacanasta.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del modelo {@link Proveedor}.
 */
class ProveedorTest {

    @Test
    void toStringDebeDevolverElNombre() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Distribuidora América");

        assertEquals("Distribuidora América", proveedor.toString());
    }
}
