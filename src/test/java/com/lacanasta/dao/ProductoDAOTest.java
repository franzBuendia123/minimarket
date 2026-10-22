package com.lacanasta.dao;

import com.lacanasta.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de {@link ProductoDAO} simulando la cadena JDBC con Mockito.
 */
class ProductoDAOTest {

    @Test
    void findByIdDebeMapearProductoConCategoria() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id_producto")).thenReturn(1);
        when(rs.getString("codigo_barras")).thenReturn("7891000012345");
        when(rs.getString("nombre")).thenReturn("Coca-Cola 1.5L");
        when(rs.getBigDecimal("precio_normal")).thenReturn(new BigDecimal("4.50"));
        when(rs.getBigDecimal("precio_descuento")).thenReturn(new BigDecimal("4.00"));
        when(rs.getInt("stock")).thenReturn(50);
        when(rs.getBoolean("estado")).thenReturn(true);
        when(rs.getInt("id_categoria")).thenReturn(2);
        when(rs.getTimestamp("fecha_creacion")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 9, 0)));
        // id_categoria presente, usuario_creacion y usuario_modificacion nulos
        when(rs.wasNull()).thenReturn(false, true, true);

        Producto producto = new ProductoDAO().findById(conn, 1);

        assertNotNull(producto);
        assertEquals(1, producto.getIdProducto());
        assertEquals("Coca-Cola 1.5L", producto.getNombre());
        assertEquals(0, producto.getPrecioNormal().compareTo(new BigDecimal("4.50")));
        assertEquals(0, producto.getPrecioDescuento().compareTo(new BigDecimal("4.00")));
        assertEquals(50, producto.getStock());
        assertTrue(producto.isEstado());
        assertEquals(Integer.valueOf(2), producto.getIdCategoria());
        assertNull(producto.getUsuarioCreacion());
        assertNull(producto.getUsuarioModificacion());
    }

    @Test
    void findByIdDebeMapearProductoSinCategoria() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id_producto")).thenReturn(2);
        when(rs.getString("nombre")).thenReturn("Fideos");
        when(rs.getBigDecimal("precio_normal")).thenReturn(new BigDecimal("3.20"));
        when(rs.getBigDecimal("precio_descuento")).thenReturn(BigDecimal.ZERO);
        when(rs.getInt("stock")).thenReturn(40);
        when(rs.getBoolean("estado")).thenReturn(true);
        // todos los campos anulables en NULL
        when(rs.wasNull()).thenReturn(true);

        Producto producto = new ProductoDAO().findById(conn, 2);

        assertNotNull(producto);
        assertEquals(2, producto.getIdProducto());
        assertNull(producto.getCodigoBarras());
        assertEquals(0, producto.getPrecioDescuento().compareTo(BigDecimal.ZERO));
        assertNull(producto.getIdCategoria());
        assertNull(producto.getFechaModificacion());
    }
}
