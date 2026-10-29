package com.lacanasta.service;

import com.lacanasta.dao.DetalleVentaDAO;
import com.lacanasta.dao.KardexDAO;
import com.lacanasta.dao.ProductoDAO;
import com.lacanasta.dao.VentaDAO;
import com.lacanasta.model.DetalleVenta;
import com.lacanasta.model.MetodoPago;
import com.lacanasta.model.Producto;
import com.lacanasta.model.Venta;
import com.lacanasta.util.ConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link VentaService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaDAO ventaDAO;

    @Mock
    private DetalleVentaDAO detalleVentaDAO;

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private KardexDAO kardexDAO;

    @Mock
    private ConnectionProvider connectionProvider;

    private VentaService ventaService;

    @BeforeEach
    void setUp() throws SQLException {
        when(connectionProvider.getConnection()).thenReturn(mock(Connection.class));
        ventaService = new VentaService(ventaDAO, detalleVentaDAO, productoDAO, kardexDAO, connectionProvider);
    }

    private Producto producto(int id, String precioNormal, String precioDescuento, int stock) {
        Producto producto = new Producto();
        producto.setIdProducto(id);
        producto.setNombre("Producto " + id);
        producto.setPrecioNormal(new BigDecimal(precioNormal));
        producto.setPrecioDescuento(new BigDecimal(precioDescuento));
        producto.setStock(stock);
        producto.setCosto(new BigDecimal("2.50"));
        return producto;
    }

    @Test
    void registrarVentaDebeCalcularTotalConPrecioDeDescuento() throws SQLException {
        Producto producto = producto(1, "4.50", "4.00", 10);
        when(productoDAO.findById(any(Connection.class), eq(1))).thenReturn(producto);
        when(ventaDAO.insert(any(Connection.class), any(Venta.class))).thenReturn(100);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdProducto(1);
        detalle.setCantidad(2);

        int idVenta = ventaService.registrarVenta(2, 3, MetodoPago.EFECTIVO, List.of(detalle));

        assertEquals(100, idVenta);

        ArgumentCaptor<Venta> captor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaDAO).insert(any(Connection.class), captor.capture());
        assertEquals(0, captor.getValue().getTotal().compareTo(new BigDecimal("8.00")));
        assertEquals(0, detalle.getPrecioUnitario().compareTo(new BigDecimal("4.00")));
        assertEquals(0, detalle.getSubtotal().compareTo(new BigDecimal("8.00")));
    }

    @Test
    void registrarVentaDebeLanzarErrorCuandoStockInsuficiente() throws SQLException {
        Producto producto = producto(1, "4.50", "0.00", 1);
        when(productoDAO.findById(any(Connection.class), eq(1))).thenReturn(producto);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdProducto(1);
        detalle.setCantidad(5);

        assertThrows(ServiceException.class,
                () -> ventaService.registrarVenta(2, 3, MetodoPago.EFECTIVO, List.of(detalle)));

        verify(ventaDAO, never()).insert(any(Connection.class), any(Venta.class));
    }

    @Test
    void registrarVentaDebeLanzarErrorCuandoProductoNoExiste() throws SQLException {
        when(productoDAO.findById(any(Connection.class), eq(99))).thenReturn(null);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdProducto(99);
        detalle.setCantidad(1);

        assertThrows(ServiceException.class,
                () -> ventaService.registrarVenta(2, 3, MetodoPago.EFECTIVO, List.of(detalle)));

        verify(ventaDAO, never()).insert(any(Connection.class), any(Venta.class));
    }

    @Test
    void debeLanzarExcepcionSiMontoRecibidoEsMenorAlTotal() throws SQLException {
        Producto producto = producto(1, "50.00", "0.00", 10);
        when(productoDAO.findById(any(Connection.class), eq(1))).thenReturn(producto);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdProducto(1);
        detalle.setCantidad(1);

        assertThrows(ServiceException.class,
                () -> ventaService.registrarVenta(2, 3, MetodoPago.EFECTIVO, List.of(detalle),
                        new BigDecimal("30.00")));
    }
}
