package com.lacanasta.service;

import com.lacanasta.dao.KardexDAO;
import com.lacanasta.dao.jdbc.KardexDAOJdbc;
import com.lacanasta.model.Kardex;
import com.lacanasta.util.ConnectionProvider;
import com.lacanasta.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de negocio para consultas del Kardex de inventario.
 */
public class KardexService {

    private final KardexDAO kardexDAO;
    private final ConnectionProvider connectionProvider;

    public KardexService() {
        this(new KardexDAOJdbc(), new DatabaseConnection());
    }

    public KardexService(KardexDAO kardexDAO, ConnectionProvider connectionProvider) {
        this.kardexDAO = kardexDAO;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Lista el historial de movimientos de un producto.
     */
    public List<Kardex> findByProducto(int idProducto) {
        try (Connection conn = connectionProvider.getConnection()) {
            return kardexDAO.findByProducto(conn, idProducto);
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar el kardex del producto", e);
        }
    }

    /**
     * Lista todos los movimientos del kardex.
     */
    public List<Kardex> findAll() {
        try (Connection conn = connectionProvider.getConnection()) {
            return kardexDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar el kardex", e);
        }
    }
}
