package com.lacanasta.dao;

import com.lacanasta.model.Kardex;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code kardex}.
 */
public interface KardexDAO {

    List<Kardex> findByProducto(Connection conn, int idProducto) throws SQLException;

    List<Kardex> findAll(Connection conn) throws SQLException;

    BigDecimal findUltimoCosto(Connection conn, int idProducto) throws SQLException;

    int insert(Connection conn, Kardex k) throws SQLException;
}
