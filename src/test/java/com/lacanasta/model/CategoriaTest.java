package com.lacanasta.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del modelo {@link Categoria}.
 */
class CategoriaTest {

    @Test
    void toStringDebeDevolverElNombre() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Bebidas");

        assertEquals("Bebidas", categoria.toString());
    }
}
