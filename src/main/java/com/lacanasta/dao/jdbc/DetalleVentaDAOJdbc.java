package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.DetalleVentaDAO;

import com.lacanasta.model.DetalleVenta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code detalle_ventas}.
 */
public class DetalleVentaDAOJdbc implements DetalleVentaDAO {

    private static final String COLUMNS =
            "id_detalle_venta, id_venta, id_producto, cantidad, precio_unitario, subtotal";

    /**
     * Lista las líneas de detalle de una venta.
     */
    public List<DetalleVenta> findByVenta(Connection conn, int idVenta) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM detalle_ventas WHERE id_venta = ?";
        List<DetalleVenta> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Inserta una línea de detalle de venta.
     */
    public int insert(Connection conn, DetalleVenta d) throws SQLException {
        String sql = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getIdVenta());
            ps.setInt(2, d.getIdProducto());
            ps.setInt(3, d.getCantidad());
            ps.setBigDecimal(4, d.getPrecioUnitario());
            ps.setBigDecimal(5, d.getSubtotal());
            return ps.executeUpdate();
        }
    }

    private static DetalleVenta map(ResultSet rs) throws SQLException {
        DetalleVenta d = new DetalleVenta();
        d.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
        d.setIdVenta(rs.getInt("id_venta"));
        d.setIdProducto(rs.getInt("id_producto"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        d.setSubtotal(rs.getBigDecimal("subtotal"));
        return d;
    }
}
