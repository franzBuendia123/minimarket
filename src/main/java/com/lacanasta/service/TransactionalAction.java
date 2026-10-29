package com.lacanasta.service;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Acción transaccional que recibe una conexión y devuelve un resultado.
 *
 * @param <T> tipo de resultado.
 */
@FunctionalInterface
public interface TransactionalAction<T> {

    /**
     * Ejecuta la acción usando la conexión transaccional proporcionada.
     *
     * @param conn conexión con autocommit desactivado.
     * @return el resultado de la acción.
     * @throws SQLException si ocurre un error de acceso a datos.
     */
    T execute(Connection conn) throws SQLException;
}
