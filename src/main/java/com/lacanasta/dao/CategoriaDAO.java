package com.lacanasta.dao;

import com.lacanasta.model.Categoria;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code categorias}.
 */
public interface CategoriaDAO {

    Categoria findById(Connection conn, int id) throws SQLException;

    List<Categoria> findAll(Connection conn) throws SQLException;

    int insert(Connection conn, Categoria c) throws SQLException;

    int update(Connection conn, Categoria c) throws SQLException;

    int delete(Connection conn, int id) throws SQLException;
}
