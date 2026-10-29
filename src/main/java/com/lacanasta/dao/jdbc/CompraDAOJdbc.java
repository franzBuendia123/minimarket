package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.CompraDAO;

import com.lacanasta.model.Compra;
import com.lacanasta.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code compras}.
 */
public class CompraDAOJdbc implements CompraDAO {

    private static final String COLUMNS = "id_compra, id_proveedor, fecha, total, id_usuario_registro";

    /**
     * Busca una compra por su identificador.
     */
    public Compra findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM compras WHERE id_compra = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Lista todas las compras, de la más reciente a la más antigua.
     */
    public List<Compra> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM compras ORDER BY fecha DESC";
        List<Compra> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Inserta una compra y devuelve su id generado.
     */
    public int insert(Connection conn, Compra c) throws SQLException {
        String sql = "INSERT INTO compras (id_proveedor, fecha, total, id_usuario_registro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getIdProveedor());
            JdbcUtil.setLocalDateTime(ps, 2, c.getFecha());
            ps.setBigDecimal(3, c.getTotal());
            JdbcUtil.setInteger(ps, 4, c.getIdUsuarioRegistro());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private static Compra map(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setIdCompra(rs.getInt("id_compra"));
        c.setIdProveedor(rs.getInt("id_proveedor"));
        c.setFecha(JdbcUtil.getLocalDateTime(rs, "fecha"));
        c.setTotal(rs.getBigDecimal("total"));
        c.setIdUsuarioRegistro(JdbcUtil.getInteger(rs, "id_usuario_registro"));
        return c;
    }
}
