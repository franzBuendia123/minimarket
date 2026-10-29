package com.lacanasta.dao;

import com.lacanasta.model.Compra;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code compras}.
 */
public interface CompraDAO {

    Compra findById(Connection conn, int id) throws SQLException;

    List<Compra> findAll(Connection conn) throws SQLException;

    int insert(Connection conn, Compra c) throws SQLException;
}
