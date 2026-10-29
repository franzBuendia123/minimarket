package com.lacanasta.dao;

import com.lacanasta.model.Caja;
import com.lacanasta.model.EstadoCaja;
import com.lacanasta.dao.jdbc.CajaDAOJdbc;
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
 * Pruebas de {@link CajaDAO} simulando la cadena JDBC con Mockito.
 */
class CajaDAOTest {

    @Test
    void findCajaAbiertaDebeMapearLaCaja() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id_caja")).thenReturn(1);
        when(rs.getBigDecimal("monto_inicial")).thenReturn(new BigDecimal("150.00"));
        when(rs.getInt("usuario_apertura")).thenReturn(2);
        when(rs.getString("estado")).thenReturn("ABIERTO");
        when(rs.wasNull()).thenReturn(true); // usuario_cierre NULL

        Caja caja = new CajaDAOJdbc().findCajaAbierta(conn);

        assertNotNull(caja);
        assertEquals(1, caja.getIdCaja());
        assertEquals(EstadoCaja.ABIERTO, caja.getEstado());
        assertEquals(0, caja.getMontoInicial().compareTo(new BigDecimal("150.00")));
        assertEquals(2, caja.getUsuarioApertura());
        assertNull(caja.getUsuarioCierre());
    }
}
