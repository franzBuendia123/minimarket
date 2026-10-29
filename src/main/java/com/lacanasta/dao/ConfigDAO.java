package com.lacanasta.dao;

import com.lacanasta.model.EmpresaConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contrato de acceso a datos de la tabla {@code empresa_configuracion}.
 */
public interface ConfigDAO {

    EmpresaConfig find(Connection conn) throws SQLException;

    int update(Connection conn, EmpresaConfig e) throws SQLException;
}
