package com.lacanasta.util;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de conexión a la base de datos.
 */
class DatabaseConnectionTest {

    /**
     * Verifica que se puede conectar y consultar la base de datos.
     */
    @Test
    void conexionExitosa() throws Exception {
        try (Connection conn = new DatabaseConnection().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            assertNotNull(conn);
            assertTrue(rs.next());
            System.out.println("CONEXION OK - Usuarios en la BD: " + rs.getInt(1));
        }
    }
}
