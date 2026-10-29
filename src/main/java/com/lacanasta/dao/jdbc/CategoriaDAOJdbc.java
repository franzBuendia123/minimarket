package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.CategoriaDAO;

import com.lacanasta.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code categorias}.
 */
public class CategoriaDAOJdbc implements CategoriaDAO {

    private static final String COLUMNS = "id_categoria, nombre, descripcion";

    /**
     * Busca una categoría por su identificador.
     */
    public Categoria findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM categorias WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Lista todas las categorías ordenadas por nombre.
     */
    public List<Categoria> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM categorias ORDER BY nombre";
        List<Categoria> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Inserta una categoría y devuelve su id generado.
     */
    public int insert(Connection conn, Categoria c) throws SQLException {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * Actualiza una categoría existente.
     */
    public int update(Connection conn, Categoria c) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ? WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setInt(3, c.getIdCategoria());
            return ps.executeUpdate();
        }
    }

    /**
     * Elimina una categoría por su identificador.
     */
    public int delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private static Categoria map(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        return c;
    }
}
