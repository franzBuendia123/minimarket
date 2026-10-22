package com.lacanasta.dao;

import com.lacanasta.model.Rol;
import com.lacanasta.model.Usuario;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de {@link UsuarioDAO} simulando la cadena JDBC con Mockito.
 */
class UsuarioDAOTest {

    @Test
    void findByUsernameDebeMapearElUsuarioYEnlazarElParametro() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id_usuario")).thenReturn(1);
        when(rs.getString("nombre")).thenReturn("Administrador");
        when(rs.getString("usuario")).thenReturn("admin");
        when(rs.getString("contrasena")).thenReturn("admin123");
        when(rs.getString("rol")).thenReturn("ADMIN");
        when(rs.getBoolean("estado")).thenReturn(true);
        when(rs.getTimestamp("fecha_creacion")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 10, 0)));

        Usuario usuario = new UsuarioDAO().findByUsername(conn, "admin");

        assertNotNull(usuario);
        assertEquals(1, usuario.getIdUsuario());
        assertEquals("Administrador", usuario.getNombre());
        assertEquals("admin", usuario.getUsuario());
        assertEquals("admin123", usuario.getContrasena());
        assertEquals(Rol.ADMIN, usuario.getRol());
        assertTrue(usuario.isEstado());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), usuario.getFechaCreacion());

        verify(conn).prepareStatement(contains("FROM usuarios"));
        verify(ps).setString(1, "admin");
    }
}
