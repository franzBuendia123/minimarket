package com.lacanasta.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Implementación de {@link ConnectionProvider} que obtiene conexiones JDBC
 * hacia MySQL leyendo los parámetros desde {@code db.properties}.
 */
public class DatabaseConnection implements ConnectionProvider {

    private static final String CONFIG_FILE = "/db.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + CONFIG_FILE + " en el classpath");
            }
            PROPERTIES.load(in);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Error al cargar " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Constructor por defecto.
     */
    public DatabaseConnection() {
    }

    /**
     * Obtiene una nueva conexión a la base de datos.
     *
     * @return una conexión activa hacia MySQL.
     * @throws SQLException si no es posible establecer la conexión.
     */
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.user"),
                PROPERTIES.getProperty("db.password")
        );
    }
}
