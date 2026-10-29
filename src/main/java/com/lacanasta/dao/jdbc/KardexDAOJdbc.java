package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.KardexDAO;

import com.lacanasta.model.Kardex;
import com.lacanasta.model.TipoMovimiento;
import com.lacanasta.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code kardex}.
 */
public class KardexDAOJdbc implements KardexDAO {

    private static final String COLUMNS =
            "id_kardex, id_producto, id_usuario_movimiento, fecha_movimiento, tipo_movimiento, "
            + "referencia, cantidad, costo_unitario, stock_anterior, stock_resultante";

    /**
     * Lista el historial de movimientos de un producto.
     */
    public List<Kardex> findByProducto(Connection conn, int idProducto) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM kardex WHERE id_producto = ? ORDER BY fecha_movimiento, id_kardex";
        List<Kardex> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lista todos los movimientos del kardex, de la fecha más reciente a la más antigua.
     */
    public List<Kardex> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM kardex ORDER BY fecha_movimiento DESC";
        List<Kardex> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Obtiene el último costo de entrada registrado para un producto.
     *
     * @return el costo unitario de la última ENTRADA, o cero si no existe.
     */
    public BigDecimal findUltimoCosto(Connection conn, int idProducto) throws SQLException {
        String sql = "SELECT costo_unitario FROM kardex WHERE id_producto = ? AND tipo_movimiento = 'ENTRADA' "
                + "ORDER BY fecha_movimiento DESC, id_kardex DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("costo_unitario") : BigDecimal.ZERO;
            }
        }
    }

    /**
     * Inserta un movimiento de kardex.
     */
    public int insert(Connection conn, Kardex k) throws SQLException {
        String sql = "INSERT INTO kardex (id_producto, id_usuario_movimiento, fecha_movimiento, tipo_movimiento, "
                + "referencia, cantidad, costo_unitario, stock_anterior, stock_resultante) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, k.getIdProducto());
            ps.setInt(2, k.getIdUsuarioMovimiento());
            JdbcUtil.setLocalDateTime(ps, 3, k.getFechaMovimiento());
            ps.setString(4, k.getTipoMovimiento().name());
            ps.setString(5, k.getReferencia());
            ps.setInt(6, k.getCantidad());
            ps.setBigDecimal(7, k.getCostoUnitario());
            ps.setInt(8, k.getStockAnterior());
            ps.setInt(9, k.getStockResultante());
            return ps.executeUpdate();
        }
    }

    private static Kardex map(ResultSet rs) throws SQLException {
        Kardex k = new Kardex();
        k.setIdKardex(rs.getInt("id_kardex"));
        k.setIdProducto(rs.getInt("id_producto"));
        k.setIdUsuarioMovimiento(rs.getInt("id_usuario_movimiento"));
        k.setFechaMovimiento(JdbcUtil.getLocalDateTime(rs, "fecha_movimiento"));
        k.setTipoMovimiento(TipoMovimiento.valueOf(rs.getString("tipo_movimiento")));
        k.setReferencia(rs.getString("referencia"));
        k.setCantidad(rs.getInt("cantidad"));
        k.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
        k.setStockAnterior(rs.getInt("stock_anterior"));
        k.setStockResultante(rs.getInt("stock_resultante"));
        return k;
    }
}
