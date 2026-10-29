package com.lacanasta.dao;

import com.lacanasta.model.DetalleVenta;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code detalle_ventas}.
 */
public interface DetalleVentaDAO {

    List<DetalleVenta> findByVenta(Connection conn, int idVenta) throws SQLException;

    int insert(Connection conn, DetalleVenta d) throws SQLException;
}
