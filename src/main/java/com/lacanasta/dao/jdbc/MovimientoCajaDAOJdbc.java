package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.MovimientoCajaDAO;

import com.lacanasta.model.MovimientoCaja;
import com.lacanasta.model.TipoMovimientoCaja;
import com.lacanasta.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code movimientos_caja}.
 */
public class MovimientoCajaDAOJdbc implements MovimientoCajaDAO {

    private static final String COLUMNS =
            "id_movimiento, id_caja, fecha, tipo, monto, descripcion, id_usuario_registro";

    /**
     * Lista los movimientos de una caja ordenados por fecha.
     */
    public List<MovimientoCaja> findByCaja(Connection conn, int idCaja) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM movimientos_caja WHERE id_caja = ? ORDER BY fecha";
        List<MovimientoCaja> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Inserta un movimiento de caja.
     */
    public int insert(Connection conn, MovimientoCaja m) throws SQLException {
        String sql = "INSERT INTO movimientos_caja (id_caja, fecha, tipo, monto, descripcion, id_usuario_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getIdCaja());
            JdbcUtil.setLocalDateTime(ps, 2, m.getFecha());
            ps.setString(3, m.getTipo().name());
            ps.setBigDecimal(4, m.getMonto());
            ps.setString(5, m.getDescripcion());
            JdbcUtil.setInteger(ps, 6, m.getIdUsuarioRegistro());
            return ps.executeUpdate();
        }
    }

    private static MovimientoCaja map(ResultSet rs) throws SQLException {
        MovimientoCaja m = new MovimientoCaja();
        m.setIdMovimiento(rs.getInt("id_movimiento"));
        m.setIdCaja(rs.getInt("id_caja"));
        m.setFecha(JdbcUtil.getLocalDateTime(rs, "fecha"));
        m.setTipo(TipoMovimientoCaja.valueOf(rs.getString("tipo")));
        m.setMonto(rs.getBigDecimal("monto"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setIdUsuarioRegistro(JdbcUtil.getInteger(rs, "id_usuario_registro"));
        return m;
    }
}
