package com.lacanasta.dao;

import com.lacanasta.model.EstadoPago;
import com.lacanasta.model.Venta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de acceso a datos de la tabla {@code ventas}.
 */
public interface VentaDAO {

    Venta findById(Connection conn, int id) throws SQLException;

    List<Venta> findAll(Connection conn) throws SQLException;

    List<Venta> findByFecha(Connection conn, LocalDate inicio, LocalDate fin) throws SQLException;

    BigDecimal sumTotalByFecha(Connection conn, LocalDate inicio, LocalDate fin) throws SQLException;

    BigDecimal sumTotalByCaja(Connection conn, int idCaja) throws SQLException;

    int insert(Connection conn, Venta v) throws SQLException;

    int updateEstadoPago(Connection conn, int idVenta, EstadoPago estadoPago) throws SQLException;

    int sumCantidadVendidaByFecha(Connection conn, LocalDate inicio, LocalDate fin) throws SQLException;
}
