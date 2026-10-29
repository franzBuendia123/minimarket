package com.lacanasta.service;

import com.lacanasta.dao.DetalleVentaDAO;
import com.lacanasta.dao.KardexDAO;
import com.lacanasta.dao.ProductoDAO;
import com.lacanasta.dao.VentaDAO;
import com.lacanasta.dao.jdbc.DetalleVentaDAOJdbc;
import com.lacanasta.dao.jdbc.KardexDAOJdbc;
import com.lacanasta.dao.jdbc.ProductoDAOJdbc;
import com.lacanasta.dao.jdbc.VentaDAOJdbc;
import com.lacanasta.model.DetalleVenta;
import com.lacanasta.model.EstadoPago;
import com.lacanasta.model.Kardex;
import com.lacanasta.model.MetodoPago;
import com.lacanasta.model.Producto;
import com.lacanasta.model.TipoMovimiento;
import com.lacanasta.model.Venta;
import com.lacanasta.util.ConnectionProvider;
import com.lacanasta.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para el registro y anulación de ventas.
 *
 * <p>El registro de una venta es transaccional: se guarda la venta, su detalle,
 * se descuenta el stock y se genera el movimiento de Kardex de forma atómica.</p>
 */
public class VentaService {

    private final VentaDAO ventaDAO;
    private final DetalleVentaDAO detalleVentaDAO;
    private final ProductoDAO productoDAO;
    private final KardexDAO kardexDAO;
    private final ConnectionProvider connectionProvider;
    private final TransactionTemplate transactionTemplate;

    /**
     * Constructor por defecto que crea las dependencias reales.
     */
    public VentaService() {
        this(new VentaDAOJdbc(), new DetalleVentaDAOJdbc(), new ProductoDAOJdbc(), new KardexDAOJdbc(), new DatabaseConnection());
    }

    /**
     * Constructor para inyección de dependencias (pruebas).
     */
    public VentaService(VentaDAO ventaDAO, DetalleVentaDAO detalleVentaDAO,
                        ProductoDAO productoDAO, KardexDAO kardexDAO, ConnectionProvider connectionProvider) {
        this.ventaDAO = ventaDAO;
        this.detalleVentaDAO = detalleVentaDAO;
        this.productoDAO = productoDAO;
        this.kardexDAO = kardexDAO;
        this.connectionProvider = connectionProvider;
        this.transactionTemplate = new TransactionTemplate(connectionProvider);
    }

    /**
     * Registra una venta completa (venta + detalle + descuento de stock + kardex).
     *
     * <p>El precio unitario se calcula automáticamente con {@link Producto#getPrecioVenta()},
     * aplicando la regla de descuento. Se valida que la cantidad no supere el stock.</p>
     *
     * @param idCajero   id del usuario que realiza la venta.
     * @param idCaja     id de la caja abierta (puede ser null).
     * @param metodoPago método de pago.
     * @param detalles   líneas de detalle (solo {@code idProducto} y {@code cantidad}).
     * @return el id de la venta registrada.
     */
    public int registrarVenta(int idCajero, Integer idCaja, MetodoPago metodoPago, List<DetalleVenta> detalles) {
        return registrarVenta(idCajero, idCaja, metodoPago, detalles, null);
    }

    /**
     * Registra una venta completa (venta + detalle + descuento de stock + kardex).
     *
     * <p>Si el pago es en efectivo, valida que el monto recibido no sea menor al total.</p>
     */
    public int registrarVenta(int idCajero, Integer idCaja, MetodoPago metodoPago,
                              List<DetalleVenta> detalles, BigDecimal montoRecibido) {
        return transactionTemplate.execute(conn -> {
            BigDecimal total = BigDecimal.ZERO;
            for (DetalleVenta d : detalles) {
                Producto producto = productoDAO.findById(conn, d.getIdProducto());
                if (producto == null) {
                    throw new ServiceException("Producto no encontrado (id " + d.getIdProducto() + ")");
                }
                if (d.getCantidad() <= 0) {
                    throw new ServiceException("Cantidad inválida para: " + producto.getNombre());
                }
                if (d.getCantidad() > producto.getStock()) {
                    throw new ServiceException("Stock insuficiente para: " + producto.getNombre());
                }
                d.setPrecioUnitario(producto.getPrecioVenta());
                d.setSubtotal(d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())));
                total = total.add(d.getSubtotal());
            }

            if (metodoPago == MetodoPago.EFECTIVO && montoRecibido != null
                    && montoRecibido.compareTo(total) < 0) {
                throw new ServiceException("El monto recibido es insuficiente");
            }

            Venta venta = new Venta();
            venta.setIdCajero(idCajero);
            venta.setIdCaja(idCaja);
            venta.setFecha(LocalDateTime.now());
            venta.setTotal(total);
            venta.setMetodoPago(metodoPago);
            venta.setEstadoPago(EstadoPago.COMPLETADA);
            int idVenta = ventaDAO.insert(conn, venta);

            for (DetalleVenta d : detalles) {
                d.setIdVenta(idVenta);
                detalleVentaDAO.insert(conn, d);

                Producto producto = productoDAO.findById(conn, d.getIdProducto());
                int stockAnterior = producto.getStock();
                int stockResultante = stockAnterior - d.getCantidad();
                productoDAO.updateStock(conn, producto.getIdProducto(), stockResultante);

                Kardex kardex = new Kardex();
                kardex.setIdProducto(producto.getIdProducto());
                kardex.setIdUsuarioMovimiento(idCajero);
                kardex.setFechaMovimiento(LocalDateTime.now());
                kardex.setTipoMovimiento(TipoMovimiento.SALIDA);
                kardex.setReferencia("VENTA_#" + idVenta);
                kardex.setCantidad(d.getCantidad());
                kardex.setCostoUnitario(producto.getCosto());
                kardex.setStockAnterior(stockAnterior);
                kardex.setStockResultante(stockResultante);
                kardexDAO.insert(conn, kardex);
            }

            return idVenta;
        });
    }

    /**
     * Lista todas las ventas, de la más reciente a la más antigua.
     */
    public List<Venta> findAll() {
        try (Connection conn = connectionProvider.getConnection()) {
            return ventaDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al listar ventas", e);
        }
    }

    /**
     * Busca una venta por su identificador.
     */
    public Venta findById(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            return ventaDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar la venta", e);
        }
    }

    /**
     * Obtiene las líneas de detalle de una venta.
     */
    public List<DetalleVenta> findDetalles(int idVenta) {
        try (Connection conn = connectionProvider.getConnection()) {
            return detalleVentaDAO.findByVenta(conn, idVenta);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el detalle de la venta", e);
        }
    }

    /**
     * Lista las ventas de un rango de fechas.
     */
    public List<Venta> findByFecha(LocalDate inicio, LocalDate fin) {
        try (Connection conn = connectionProvider.getConnection()) {
            return ventaDAO.findByFecha(conn, inicio, fin);
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar ventas por fecha", e);
        }
    }

    /**
     * Calcula el total de ventas completadas de un rango de fechas.
     */
    public BigDecimal sumTotalByFecha(LocalDate inicio, LocalDate fin) {
        try (Connection conn = connectionProvider.getConnection()) {
            return ventaDAO.sumTotalByFecha(conn, inicio, fin);
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar el total de ventas", e);
        }
    }

    /**
     * Calcula la cantidad total de unidades vendidas en un rango de fechas.
     */
    public int sumCantidadVendidaByFecha(LocalDate inicio, LocalDate fin) {
        try (Connection conn = connectionProvider.getConnection()) {
            return ventaDAO.sumCantidadVendidaByFecha(conn, inicio, fin);
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar la cantidad vendida", e);
        }
    }

    /**
     * Anula una venta, devolviendo el stock y registrando el movimiento en Kardex.
     *
     * @param idVenta   id de la venta a anular.
     * @param idUsuario id del usuario que realiza la anulación.
     */
    public void anularVenta(int idVenta, int idUsuario) {
        transactionTemplate.execute(conn -> {
            Venta venta = ventaDAO.findById(conn, idVenta);
            if (venta == null) {
                throw new ServiceException("Venta no encontrada");
            }
            if (venta.getEstadoPago() == EstadoPago.ANULADA) {
                throw new ServiceException("La venta ya está anulada");
            }

            List<DetalleVenta> detalles = detalleVentaDAO.findByVenta(conn, idVenta);
            for (DetalleVenta d : detalles) {
                Producto producto = productoDAO.findById(conn, d.getIdProducto());
                int stockAnterior = producto.getStock();
                int stockResultante = stockAnterior + d.getCantidad();
                productoDAO.updateStock(conn, producto.getIdProducto(), stockResultante);

                Kardex kardex = new Kardex();
                kardex.setIdProducto(producto.getIdProducto());
                kardex.setIdUsuarioMovimiento(idUsuario);
                kardex.setFechaMovimiento(LocalDateTime.now());
                kardex.setTipoMovimiento(TipoMovimiento.ENTRADA);
                kardex.setReferencia("ANULACION_VENTA_#" + idVenta);
                kardex.setCantidad(d.getCantidad());
                kardex.setCostoUnitario(d.getPrecioUnitario());
                kardex.setStockAnterior(stockAnterior);
                kardex.setStockResultante(stockResultante);
                kardexDAO.insert(conn, kardex);
            }

            ventaDAO.updateEstadoPago(conn, idVenta, EstadoPago.ANULADA);

            return null;
        });
    }
}
