package com.lacanasta.dao;

import com.lacanasta.model.DetalleCompra;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code detalle_compras}.
 */
public interface DetalleCompraDAO {

    List<DetalleCompra> findByCompra(Connection conn, int idCompra) throws SQLException;

    int insert(Connection conn, DetalleCompra d) throws SQLException;
}
