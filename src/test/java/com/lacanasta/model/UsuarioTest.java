package com.lacanasta.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del modelo {@link Usuario}.
 */
class UsuarioTest {

    @Test
    void debePermitirAsignarYLeerSusAtributos() {
        LocalDateTime fecha = LocalDateTime.now();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("Administrador");
        usuario.setUsuario("admin");
        usuario.setContrasena("secreta");
        usuario.setRol(Rol.ADMIN);
        usuario.setEstado(true);
        usuario.setFechaCreacion(fecha);

        assertEquals(1, usuario.getIdUsuario());
        assertEquals("Administrador", usuario.getNombre());
        assertEquals("admin", usuario.getUsuario());
        assertEquals("secreta", usuario.getContrasena());
        assertEquals(Rol.ADMIN, usuario.getRol());
        assertTrue(usuario.isEstado());
        assertEquals(fecha, usuario.getFechaCreacion());
    }

    @Test
    void rolPorDefectoEsNuloEnUnUsuarioNuevo() {
        Usuario usuario = new Usuario();

        assertEquals(null, usuario.getRol());
        assertEquals(false, usuario.isEstado());
    }
}
