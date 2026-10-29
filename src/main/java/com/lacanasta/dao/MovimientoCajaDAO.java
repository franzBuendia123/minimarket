package com.lacanasta.dao;

import com.lacanasta.model.MovimientoCaja;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code movimientos_caja}.
 */
public interface MovimientoCajaDAO {

    List<MovimientoCaja> findByCaja(Connection conn, int idCaja) throws SQLException;

    int insert(Connection conn, MovimientoCaja m) throws SQLException;
}
