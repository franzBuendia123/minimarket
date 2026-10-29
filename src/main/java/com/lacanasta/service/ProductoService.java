package com.lacanasta.service;

import com.lacanasta.dao.ProductoDAO;
import com.lacanasta.dao.jdbc.ProductoDAOJdbc;
import com.lacanasta.model.Producto;
import com.lacanasta.util.ConnectionProvider;
import com.lacanasta.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para la gestión de productos.
 */
public class ProductoService {

    private final ProductoDAO productoDAO;
    private final ConnectionProvider connectionProvider;

    public ProductoService() {
        this(new ProductoDAOJdbc(), new DatabaseConnection());
    }

    public ProductoService(ProductoDAO productoDAO, ConnectionProvider connectionProvider) {
        this.productoDAO = productoDAO;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Lista todos los productos.
     */
    public List<Producto> findAll() {
        try (Connection conn = connectionProvider.getConnection()) {
            return productoDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al listar productos", e);
        }
    }

    /**
     * Busca un producto por su identificador.
     */
    public Producto findById(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            return productoDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el producto", e);
        }
    }

    /**
     * Busca un producto por su código de barras.
     */
    public Producto findByCodigoBarras(String codigoBarras) {
        try (Connection conn = connectionProvider.getConnection()) {
            return productoDAO.findByCodigoBarras(conn, codigoBarras);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el producto por código de barras", e);
        }
    }

    /**
     * Busca productos cuyo nombre coincida parcialmente con el texto dado.
     */
    public List<Producto> search(String nombre) {
        try (Connection conn = connectionProvider.getConnection()) {
            return productoDAO.findByNombre(conn, nombre);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar productos", e);
        }
    }

    /**
     * Lista los productos de una categoría.
     */
    public List<Producto> findByCategoria(int idCategoria) {
        try (Connection conn = connectionProvider.getConnection()) {
            return productoDAO.findByCategoria(conn, idCategoria);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar productos por categoría", e);
        }
    }

    /**
     * Lista los productos con stock bajo (igual o menor al límite).
     */
    public List<Producto> findByStockBajo(int limite) {
        try (Connection conn = connectionProvider.getConnection()) {
            return productoDAO.findByStockBajo(conn, limite);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar productos con stock bajo", e);
        }
    }

    /**
     * Guarda un producto (inserta o actualiza) estableciendo las fechas de auditoría.
     */
    public void save(Producto producto) {
        try (Connection conn = connectionProvider.getConnection()) {
            if (producto.getIdProducto() == 0) {
                if (producto.getFechaCreacion() == null) {
                    producto.setFechaCreacion(LocalDateTime.now());
                }
                productoDAO.insert(conn, producto);
            } else {
                producto.setFechaModificacion(LocalDateTime.now());
                productoDAO.update(conn, producto);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar el producto", e);
        }
    }

    /**
     * Elimina un producto por su identificador.
     */
    public void delete(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            productoDAO.delete(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el producto", e);
        }
    }
}
