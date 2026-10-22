package com.lacanasta.service;

import com.lacanasta.dao.CompraDAO;
import com.lacanasta.dao.DetalleCompraDAO;
import com.lacanasta.dao.KardexDAO;
import com.lacanasta.dao.ProductoDAO;
import com.lacanasta.model.Compra;
import com.lacanasta.model.DetalleCompra;
import com.lacanasta.model.Kardex;
import com.lacanasta.model.Producto;
import com.lacanasta.model.TipoMovimiento;
import com.lacanasta.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para el registro de compras.
 *
 * <p>El registro de una compra es transaccional: se guarda la compra, su detalle,
 * se incrementa el stock y se genera el movimiento de Kardex de forma atómica.</p>
 */
public class CompraService {

    private final CompraDAO compraDAO;
    private final DetalleCompraDAO detalleCompraDAO;
    private final ProductoDAO productoDAO;
    private final KardexDAO kardexDAO;

    /**
     * Constructor por defecto que crea las dependencias reales.
     */
    public CompraService() {
        this(new CompraDAO(), new DetalleCompraDAO(), new ProductoDAO(), new KardexDAO());
    }

    /**
     * Constructor para inyección de dependencias (pruebas).
     */
    public CompraService(CompraDAO compraDAO, DetalleCompraDAO detalleCompraDAO,
                         ProductoDAO productoDAO, KardexDAO kardexDAO) {
        this.compraDAO = compraDAO;
        this.detalleCompraDAO = detalleCompraDAO;
        this.productoDAO = productoDAO;
        this.kardexDAO = kardexDAO;
    }

    /**
     * Registra una compra completa (compra + detalle + incremento de stock + kardex).
     *
     * @param idProveedor        id del proveedor.
     * @param idUsuarioRegistro  id del usuario que registra la compra.
     * @param detalles           líneas de detalle (con costo unitario).
     * @return el id de la compra registrada.
     */
    public int registrarCompra(int idProveedor, Integer idUsuarioRegistro, List<DetalleCompra> detalles) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal total = BigDecimal.ZERO;
                for (DetalleCompra d : detalles) {
                    BigDecimal subtotal = d.getCostoUnitario()
                            .multiply(BigDecimal.valueOf(d.getCantidad()));
                    d.setSubtotal(subtotal);
                    total = total.add(subtotal);
                }

                Compra compra = new Compra();
                compra.setIdProveedor(idProveedor);
                compra.setFecha(LocalDateTime.now());
                compra.setTotal(total);
                compra.setIdUsuarioRegistro(idUsuarioRegistro);
                int idCompra = compraDAO.insert(conn, compra);

                for (DetalleCompra d : detalles) {
                    d.setIdCompra(idCompra);
                    detalleCompraDAO.insert(conn, d);

                    Producto producto = productoDAO.findById(conn, d.getIdProducto());
                    if (producto == null) {
                        throw new ServiceException("Producto no encontrado (id " + d.getIdProducto() + ")");
                    }
                    int stockAnterior = producto.getStock();
                    int stockResultante = stockAnterior + d.getCantidad();
                    productoDAO.updateStock(conn, producto.getIdProducto(), stockResultante);

                    Kardex kardex = new Kardex();
                    kardex.setIdProducto(producto.getIdProducto());
                    kardex.setIdUsuarioMovimiento(idUsuarioRegistro != null ? idUsuarioRegistro : 0);
                    kardex.setFechaMovimiento(LocalDateTime.now());
                    kardex.setTipoMovimiento(TipoMovimiento.ENTRADA);
                    kardex.setReferencia("COMPRA_#" + idCompra);
                    kardex.setCantidad(d.getCantidad());
                    kardex.setCostoUnitario(d.getCostoUnitario());
                    kardex.setStockAnterior(stockAnterior);
                    kardex.setStockResultante(stockResultante);
                    kardexDAO.insert(conn, kardex);
                }

                conn.commit();
                return idCompra;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof ServiceException) {
                    throw (ServiceException) e;
                }
                throw new ServiceException("Error al registrar la compra", e);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error de conexión al registrar la compra", e);
        }
    }

    /**
     * Lista todas las compras.
     */
    public List<Compra> findAll() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return compraDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al listar compras", e);
        }
    }

    /**
     * Busca una compra por su identificador.
     */
    public Compra findById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return compraDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar la compra", e);
        }
    }

    /**
     * Obtiene las líneas de detalle de una compra.
     */
    public List<DetalleCompra> findDetalles(int idCompra) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return detalleCompraDAO.findByCompra(conn, idCompra);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el detalle de la compra", e);
        }
    }
}
