package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.VentaDAO;

import com.lacanasta.model.EstadoPago;
import com.lacanasta.model.MetodoPago;
import com.lacanasta.model.Venta;
import com.lacanasta.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code ventas}.
 */
public class VentaDAOJdbc implements VentaDAO {

    private static final String COLUMNS =
            "id_venta, id_cajero, id_caja, fecha, total, metodo_pago, estado_pago";

    /**
     * Busca una venta por su identificador.
     */
    public Venta findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM ventas WHERE id_venta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Lista todas las ventas, de la más reciente a la más antigua.
     */
    public List<Venta> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM ventas ORDER BY fecha DESC";
        List<Venta> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Inserta una venta y devuelve su id generado.
     */
    public int insert(Connection conn, Venta v) throws SQLException {
        String sql = "INSERT INTO ventas (id_cajero, id_caja, fecha, total, metodo_pago, estado_pago) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getIdCajero());
            JdbcUtil.setInteger(ps, 2, v.getIdCaja());
            JdbcUtil.setLocalDateTime(ps, 3, v.getFecha());
            ps.setBigDecimal(4, v.getTotal());
            ps.setString(5, v.getMetodoPago().name());
            ps.setString(6, v.getEstadoPago().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * Cambia el estado de pago de una venta (para anulación).
     */
    public int updateEstadoPago(Connection conn, int idVenta, EstadoPago estadoPago) throws SQLException {
        String sql = "UPDATE ventas SET estado_pago = ? WHERE id_venta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estadoPago.name());
            ps.setInt(2, idVenta);
            return ps.executeUpdate();
        }
    }

    /**
     * Lista las ventas en un rango de fechas (inicio inclusivo, fin exclusivo).
     */
    public List<Venta> findByFecha(Connection conn, LocalDate inicio, LocalDate fin) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM ventas WHERE fecha >= ? AND fecha < ? ORDER BY fecha DESC";
        List<Venta> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fin.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Calcula la suma de los totales de las ventas completadas en un rango de fechas.
     */
    public BigDecimal sumTotalByFecha(Connection conn, LocalDate inicio, LocalDate fin) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas "
                + "WHERE fecha >= ? AND fecha < ? AND estado_pago = 'COMPLETADA'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fin.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    /**
     * Calcula la suma de los totales de las ventas completadas de una caja.
     */
    public BigDecimal sumTotalByCaja(Connection conn, int idCaja) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE id_caja = ? AND estado_pago = 'COMPLETADA'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private static Venta map(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setIdCajero(rs.getInt("id_cajero"));
        v.setIdCaja(JdbcUtil.getInteger(rs, "id_caja"));
        v.setFecha(JdbcUtil.getLocalDateTime(rs, "fecha"));
        v.setTotal(rs.getBigDecimal("total"));
        v.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
        v.setEstadoPago(EstadoPago.valueOf(rs.getString("estado_pago")));
        return v;
    }
}
