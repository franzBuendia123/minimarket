package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.DetalleCompraDAO;

import com.lacanasta.model.DetalleCompra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code detalle_compras}.
 */
public class DetalleCompraDAOJdbc implements DetalleCompraDAO {

    private static final String COLUMNS =
            "id_detalle_compra, id_compra, id_producto, cantidad, costo_unitario, subtotal";

    /**
     * Lista las líneas de detalle de una compra.
     */
    public List<DetalleCompra> findByCompra(Connection conn, int idCompra) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM detalle_compras WHERE id_compra = ?";
        List<DetalleCompra> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Inserta una línea de detalle de compra.
     */
    public int insert(Connection conn, DetalleCompra d) throws SQLException {
        String sql = "INSERT INTO detalle_compras (id_compra, id_producto, cantidad, costo_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getIdCompra());
            ps.setInt(2, d.getIdProducto());
            ps.setInt(3, d.getCantidad());
            ps.setBigDecimal(4, d.getCostoUnitario());
            ps.setBigDecimal(5, d.getSubtotal());
            return ps.executeUpdate();
        }
    }

    private static DetalleCompra map(ResultSet rs) throws SQLException {
        DetalleCompra d = new DetalleCompra();
        d.setIdDetalleCompra(rs.getInt("id_detalle_compra"));
        d.setIdCompra(rs.getInt("id_compra"));
        d.setIdProducto(rs.getInt("id_producto"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
        d.setSubtotal(rs.getBigDecimal("subtotal"));
        return d;
    }
}
