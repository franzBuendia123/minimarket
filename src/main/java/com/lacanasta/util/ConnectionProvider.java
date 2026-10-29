package com.lacanasta.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstracción para obtener conexiones JDBC.
 *
 * <p>Permite desacoplar los servicios de una implementación concreta y
 * facilita las pruebas mediante mocks.</p>
 */
public interface ConnectionProvider {

    /**
     * Obtiene una conexión a la base de datos.
     *
     * @return una conexión activa.
     * @throws SQLException si no es posible establecer la conexión.
     */
    Connection getConnection() throws SQLException;
}
