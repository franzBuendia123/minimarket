package com.lacanasta.dao;

import com.lacanasta.model.Caja;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code caja}.
 */
public interface CajaDAO {

    Caja findById(Connection conn, int id) throws SQLException;

    Caja findCajaAbierta(Connection conn) throws SQLException;

    List<Caja> findAll(Connection conn) throws SQLException;

    int insert(Connection conn, Caja c) throws SQLException;

    int update(Connection conn, Caja c) throws SQLException;
}
