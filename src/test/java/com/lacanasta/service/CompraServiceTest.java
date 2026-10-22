package com.lacanasta.service;

import com.lacanasta.dao.CompraDAO;
import com.lacanasta.dao.DetalleCompraDAO;
import com.lacanasta.dao.KardexDAO;
import com.lacanasta.dao.ProductoDAO;
import com.lacanasta.model.Compra;
import com.lacanasta.model.DetalleCompra;
import com.lacanasta.model.Producto;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link CompraService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraDAO compraDAO;

    @Mock
    private DetalleCompraDAO detalleCompraDAO;

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private KardexDAO kardexDAO;

    private CompraService compraService;
    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(compraDAO, detalleCompraDAO, productoDAO, kardexDAO);
        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(mock(Connection.class));
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }

    @Test
    void registrarCompraDebeCalcularTotalConCostoPorCantidad() throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setStock(5);
        when(productoDAO.findById(any(Connection.class), eq(1))).thenReturn(producto);
        when(compraDAO.insert(any(Connection.class), any(Compra.class))).thenReturn(50);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setIdProducto(1);
        detalle.setCantidad(3);
        detalle.setCostoUnitario(new BigDecimal("2.50"));

        int idCompra = compraService.registrarCompra(1, 2, List.of(detalle));

        assertEquals(50, idCompra);

        ArgumentCaptor<Compra> captor = ArgumentCaptor.forClass(Compra.class);
        verify(compraDAO).insert(any(Connection.class), captor.capture());
        assertEquals(0, captor.getValue().getTotal().compareTo(new BigDecimal("7.50")));
        assertEquals(0, detalle.getSubtotal().compareTo(new BigDecimal("7.50")));
    }
}
