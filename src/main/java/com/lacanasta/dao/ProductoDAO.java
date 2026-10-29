package com.lacanasta.dao;

import com.lacanasta.model.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code productos}.
 */
public interface ProductoDAO {

    Producto findById(Connection conn, int id) throws SQLException;

    Producto findByCodigoBarras(Connection conn, String codigoBarras) throws SQLException;

    List<Producto> findByStockBajo(Connection conn, int limite) throws SQLException;

    List<Producto> findAll(Connection conn) throws SQLException;

    List<Producto> findByNombre(Connection conn, String nombre) throws SQLException;

    List<Producto> findByCategoria(Connection conn, int idCategoria) throws SQLException;

    int insert(Connection conn, Producto p) throws SQLException;

    int update(Connection conn, Producto p) throws SQLException;

    int updateStock(Connection conn, int idProducto, int nuevoStock) throws SQLException;

    int updateCosto(Connection conn, int idProducto, BigDecimal costo) throws SQLException;

    int delete(Connection conn, int id) throws SQLException;
}
