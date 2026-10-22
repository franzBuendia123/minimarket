package com.lacanasta.service;

import com.lacanasta.dao.CajaDAO;
import com.lacanasta.dao.MovimientoCajaDAO;
import com.lacanasta.dao.VentaDAO;
import com.lacanasta.model.Caja;
import com.lacanasta.model.EstadoCaja;
import com.lacanasta.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link CajaService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CajaServiceTest {

    @Mock
    private CajaDAO cajaDAO;

    @Mock
    private MovimientoCajaDAO movimientoCajaDAO;

    @Mock
    private VentaDAO ventaDAO;

    private CajaService cajaService;
    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setUp() {
        cajaService = new CajaService(cajaDAO, movimientoCajaDAO, ventaDAO);
        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(mock(Connection.class));
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }

    @Test
    void cerrarCajaDebeCalcularMontoEsperadoYDiferencia() throws SQLException {
        Caja caja = new Caja();
        caja.setIdCaja(1);
        caja.setMontoInicial(new BigDecimal("100.00"));
        when(cajaDAO.findCajaAbierta(any(Connection.class))).thenReturn(caja);
        when(ventaDAO.sumTotalByCaja(any(Connection.class), eq(1))).thenReturn(new BigDecimal("250.00"));

        cajaService.cerrarCaja(new BigDecimal("360.00"), 2);

        ArgumentCaptor<Caja> captor = ArgumentCaptor.forClass(Caja.class);
        verify(cajaDAO).update(any(Connection.class), captor.capture());
        Caja actualizada = captor.getValue();
        assertEquals(0, actualizada.getMontoEsperado().compareTo(new BigDecimal("350.00")));
        assertEquals(0, actualizada.getDiferencia().compareTo(new BigDecimal("10.00")));
        assertEquals(EstadoCaja.CERRADO, actualizada.getEstado());
    }

    @Test
    void cerrarCajaDebeLanzarErrorCuandoNoHayCajaAbierta() throws SQLException {
        when(cajaDAO.findCajaAbierta(any(Connection.class))).thenReturn(null);

        assertThrows(ServiceException.class, () -> cajaService.cerrarCaja(new BigDecimal("100.00"), 2));
    }

    @Test
    void abrirCajaDebeLanzarErrorCuandoYaExisteCajaAbierta() throws SQLException {
        when(cajaDAO.findCajaAbierta(any(Connection.class))).thenReturn(new Caja());

        assertThrows(ServiceException.class, () -> cajaService.abrirCaja(new BigDecimal("50.00"), 1));
    }
}
