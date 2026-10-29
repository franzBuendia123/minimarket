package com.lacanasta.service;

import com.lacanasta.util.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Plantilla que encapsula el ciclo de vida de una transacción JDBC:
 * apertura de conexión, {@code setAutoCommit(false)}, commit y rollback.
 *
 * <p>Elimina el boilerplate repetido en los servicios transaccionales.</p>
 */
public class TransactionTemplate {

    private final ConnectionProvider connectionProvider;

    /**
     * Constructor.
     *
     * @param connectionProvider proveedor de conexiones.
     */
    public TransactionTemplate(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    /**
     * Ejecuta una acción de forma transaccional.
     *
     * @param action acción a ejecutar.
     * @param <T>    tipo de resultado.
     * @return el resultado de la acción.
     */
    public <T> T execute(TransactionalAction<T> action) {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = action.execute(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof ServiceException) {
                    throw (ServiceException) e;
                }
                throw new ServiceException("Error al ejecutar la transacción", e);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error de conexión", e);
        }
    }
}
