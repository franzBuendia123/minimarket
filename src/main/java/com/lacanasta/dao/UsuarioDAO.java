package com.lacanasta.dao;

import com.lacanasta.model.Usuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code usuarios}.
 */
public interface UsuarioDAO {

    Usuario findById(Connection conn, int id) throws SQLException;

    Usuario findByUsername(Connection conn, String username) throws SQLException;

    List<Usuario> findAll(Connection conn) throws SQLException;

    int insert(Connection conn, Usuario u) throws SQLException;

    int update(Connection conn, Usuario u) throws SQLException;

    int delete(Connection conn, int id) throws SQLException;
}
