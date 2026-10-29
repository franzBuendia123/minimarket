package com.lacanasta.service;

import com.lacanasta.dao.ConfigDAO;
import com.lacanasta.dao.jdbc.ConfigDAOJdbc;
import com.lacanasta.model.EmpresaConfig;
import com.lacanasta.util.ConnectionProvider;
import com.lacanasta.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Lógica de negocio para la configuración de la empresa.
 */
public class ConfigService {

    private final ConfigDAO configDAO;
    private final ConnectionProvider connectionProvider;

    public ConfigService() {
        this(new ConfigDAOJdbc(), new DatabaseConnection());
    }

    public ConfigService(ConfigDAO configDAO, ConnectionProvider connectionProvider) {
        this.configDAO = configDAO;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Obtiene la configuración única de la empresa.
     */
    public EmpresaConfig get() {
        try (Connection conn = connectionProvider.getConnection()) {
            return configDAO.find(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener la configuración de la empresa", e);
        }
    }

    /**
     * Actualiza la configuración de la empresa.
     */
    public void update(EmpresaConfig config) {
        try (Connection conn = connectionProvider.getConnection()) {
            configDAO.update(conn, config);
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar la configuración de la empresa", e);
        }
    }
}
