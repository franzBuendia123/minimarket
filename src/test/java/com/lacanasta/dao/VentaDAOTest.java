package com.lacanasta.dao;

import com.lacanasta.model.EstadoPago;
import com.lacanasta.model.MetodoPago;
import com.lacanasta.model.Venta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de {@link VentaDAO} simulando la cadena JDBC con Mockito.
 */
class VentaDAOTest {

    @Test
    void findByIdDebeMapearVentaConEnumsYCajaNula() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id_venta")).thenReturn(10);
        when(rs.getInt("id_cajero")).thenReturn(2);
        when(rs.getBigDecimal("total")).thenReturn(new BigDecimal("9.30"));
        when(rs.getString("metodo_pago")).thenReturn("EFECTIVO");
        when(rs.getString("estado_pago")).thenReturn("COMPLETADA");
        when(rs.wasNull()).thenReturn(true); // id_caja NULL

        Venta venta = new VentaDAO().findById(conn, 10);

        assertNotNull(venta);
        assertEquals(10, venta.getIdVenta());
        assertEquals(2, venta.getIdCajero());
        assertNull(venta.getIdCaja());
        assertEquals(0, venta.getTotal().compareTo(new BigDecimal("9.30")));
        assertEquals(MetodoPago.EFECTIVO, venta.getMetodoPago());
        assertEquals(EstadoPago.COMPLETADA, venta.getEstadoPago());
    }
}
