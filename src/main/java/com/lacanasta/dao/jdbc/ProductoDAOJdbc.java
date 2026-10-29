package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.ProductoDAO;

import com.lacanasta.model.Producto;
import com.lacanasta.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code productos}.
 */
public class ProductoDAOJdbc implements ProductoDAO {

    private static final String COLUMNS =
            "id_producto, codigo_barras, nombre, precio_normal, precio_descuento, costo, stock, estado, "
            + "id_categoria, usuario_creacion, fecha_creacion, usuario_modificacion, fecha_modificacion";

    /**
     * Busca un producto por su identificador.
     */
    public Producto findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM productos WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Busca un producto por su código de barras.
     */
    public Producto findByCodigoBarras(Connection conn, String codigoBarras) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM productos WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoBarras);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Lista todos los productos ordenados por nombre.
     */
    public List<Producto> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM productos ORDER BY nombre";
        List<Producto> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Busca productos cuyo nombre coincida parcialmente con el texto dado.
     */
    public List<Producto> findByNombre(Connection conn, String nombre) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM productos WHERE nombre LIKE ? ORDER BY nombre";
        List<Producto> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lista los productos que pertenecen a una categoría.
     */
    public List<Producto> findByCategoria(Connection conn, int idCategoria) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM productos WHERE id_categoria = ? ORDER BY nombre";
        List<Producto> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lista los productos con stock igual o menor al límite indicado (para alertas).
     */
    public List<Producto> findByStockBajo(Connection conn, int limite) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM productos WHERE stock <= ? AND estado = 1 ORDER BY stock";
        List<Producto> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Inserta un producto y devuelve su id generado.
     */
    public int insert(Connection conn, Producto p) throws SQLException {
        String sql = "INSERT INTO productos (codigo_barras, nombre, precio_normal, precio_descuento, costo, stock, estado, "
                + "id_categoria, usuario_creacion, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigoBarras());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecioNormal());
            ps.setBigDecimal(4, p.getPrecioDescuento());
            ps.setBigDecimal(5, p.getCosto());
            ps.setInt(6, p.getStock());
            ps.setBoolean(7, p.isEstado());
            JdbcUtil.setInteger(ps, 8, p.getIdCategoria());
            JdbcUtil.setInteger(ps, 9, p.getUsuarioCreacion());
            JdbcUtil.setLocalDateTime(ps, 10, p.getFechaCreacion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * Actualiza los datos de un producto existente.
     */
    public int update(Connection conn, Producto p) throws SQLException {
        String sql = "UPDATE productos SET codigo_barras = ?, nombre = ?, precio_normal = ?, precio_descuento = ?, costo = ?, "
                + "stock = ?, estado = ?, id_categoria = ?, usuario_modificacion = ?, fecha_modificacion = ? "
                + "WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigoBarras());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecioNormal());
            ps.setBigDecimal(4, p.getPrecioDescuento());
            ps.setBigDecimal(5, p.getCosto());
            ps.setInt(6, p.getStock());
            ps.setBoolean(7, p.isEstado());
            JdbcUtil.setInteger(ps, 8, p.getIdCategoria());
            JdbcUtil.setInteger(ps, 9, p.getUsuarioModificacion());
            JdbcUtil.setLocalDateTime(ps, 10, p.getFechaModificacion());
            ps.setInt(11, p.getIdProducto());
            return ps.executeUpdate();
        }
    }

    /**
     * Actualiza únicamente el stock de un producto.
     */
    public int updateStock(Connection conn, int idProducto, int nuevoStock) throws SQLException {
        String sql = "UPDATE productos SET stock = ? WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, idProducto);
            return ps.executeUpdate();
        }
    }

    /**
     * Actualiza únicamente el costo de un producto.
     */
    public int updateCosto(Connection conn, int idProducto, BigDecimal costo) throws SQLException {
        String sql = "UPDATE productos SET costo = ? WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, costo);
            ps.setInt(2, idProducto);
            return ps.executeUpdate();
        }
    }

    /**
     * Elimina un producto por su identificador.
     */
    public int delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private static Producto map(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecioNormal(rs.getBigDecimal("precio_normal"));
        p.setPrecioDescuento(rs.getBigDecimal("precio_descuento"));
        p.setCosto(rs.getBigDecimal("costo"));
        p.setStock(rs.getInt("stock"));
        p.setEstado(rs.getBoolean("estado"));
        p.setIdCategoria(JdbcUtil.getInteger(rs, "id_categoria"));
        p.setUsuarioCreacion(JdbcUtil.getInteger(rs, "usuario_creacion"));
        p.setFechaCreacion(JdbcUtil.getLocalDateTime(rs, "fecha_creacion"));
        p.setUsuarioModificacion(JdbcUtil.getInteger(rs, "usuario_modificacion"));
        p.setFechaModificacion(JdbcUtil.getLocalDateTime(rs, "fecha_modificacion"));
        return p;
    }
}
