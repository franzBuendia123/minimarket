package com.lacanasta.service;

import com.lacanasta.dao.ProveedorDAO;
import com.lacanasta.dao.jdbc.ProveedorDAOJdbc;
import com.lacanasta.model.Proveedor;
import com.lacanasta.util.ConnectionProvider;
import com.lacanasta.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para la gestión de proveedores.
 */
public class ProveedorService {

    private final ProveedorDAO proveedorDAO;
    private final ConnectionProvider connectionProvider;

    public ProveedorService() {
        this(new ProveedorDAOJdbc(), new DatabaseConnection());
    }

    public ProveedorService(ProveedorDAO proveedorDAO, ConnectionProvider connectionProvider) {
        this.proveedorDAO = proveedorDAO;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Lista todos los proveedores.
     */
    public List<Proveedor> findAll() {
        try (Connection conn = connectionProvider.getConnection()) {
            return proveedorDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al listar proveedores", e);
        }
    }

    /**
     * Busca un proveedor por su identificador.
     */
    public Proveedor findById(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            return proveedorDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el proveedor", e);
        }
    }

    /**
     * Guarda un proveedor (inserta o actualiza) estableciendo las fechas de auditoría.
     */
    public void save(Proveedor proveedor) {
        try (Connection conn = connectionProvider.getConnection()) {
            if (proveedor.getIdProveedor() == 0) {
                if (proveedor.getFechaCreacion() == null) {
                    proveedor.setFechaCreacion(LocalDateTime.now());
                }
                proveedorDAO.insert(conn, proveedor);
            } else {
                proveedor.setFechaModificacion(LocalDateTime.now());
                proveedorDAO.update(conn, proveedor);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar el proveedor", e);
        }
    }

    /**
     * Elimina un proveedor por su identificador.
     */
    public void delete(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            proveedorDAO.delete(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el proveedor", e);
        }
    }
}
