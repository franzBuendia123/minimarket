package com.lacanasta.dao;

import com.lacanasta.model.Proveedor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code proveedores}.
 */
public interface ProveedorDAO {

    Proveedor findById(Connection conn, int id) throws SQLException;

    List<Proveedor> findAll(Connection conn) throws SQLException;

    int insert(Connection conn, Proveedor p) throws SQLException;

    int update(Connection conn, Proveedor p) throws SQLException;

    int delete(Connection conn, int id) throws SQLException;
}
