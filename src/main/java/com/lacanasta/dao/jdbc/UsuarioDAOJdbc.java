package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.UsuarioDAO;

import com.lacanasta.model.Rol;
import com.lacanasta.model.Usuario;
import com.lacanasta.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code usuarios}.
 */
public class UsuarioDAOJdbc implements UsuarioDAO {

    private static final String COLUMNS = "id_usuario, nombre, usuario, contrasena, rol, estado, fecha_creacion";

    /**
     * Busca un usuario por su identificador.
     */
    public Usuario findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Busca un usuario por su nombre de usuario (login).
     */
    public Usuario findByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM usuarios WHERE usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Lista todos los usuarios.
     */
    public List<Usuario> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM usuarios ORDER BY id_usuario";
        List<Usuario> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Inserta un nuevo usuario y devuelve su id generado.
     */
    public int insert(Connection conn, Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, usuario, contrasena, rol, estado, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getContrasena());
            ps.setString(4, u.getRol().name());
            ps.setBoolean(5, u.isEstado());
            JdbcUtil.setLocalDateTime(ps, 6, u.getFechaCreacion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * Actualiza los datos de un usuario existente.
     */
    public int update(Connection conn, Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, usuario = ?, contrasena = ?, rol = ?, estado = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getContrasena());
            ps.setString(4, u.getRol().name());
            ps.setBoolean(5, u.isEstado());
            ps.setInt(6, u.getIdUsuario());
            return ps.executeUpdate();
        }
    }

    /**
     * Elimina un usuario por su identificador.
     */
    public int delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private static Usuario map(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setUsuario(rs.getString("usuario"));
        u.setContrasena(rs.getString("contrasena"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setEstado(rs.getBoolean("estado"));
        u.setFechaCreacion(JdbcUtil.getLocalDateTime(rs, "fecha_creacion"));
        return u;
    }
}
