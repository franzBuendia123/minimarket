package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.ProveedorDAO;

import com.lacanasta.model.Proveedor;
import com.lacanasta.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code proveedores}.
 */
public class ProveedorDAOJdbc implements ProveedorDAO {

    private static final String COLUMNS =
            "id_proveedor, nombre, contacto, telefono, direccion, "
            + "usuario_creacion, fecha_creacion, usuario_modificacion, fecha_modificacion";

    /**
     * Busca un proveedor por su identificador.
     */
    public Proveedor findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM proveedores WHERE id_proveedor = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Lista todos los proveedores ordenados por nombre.
     */
    public List<Proveedor> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM proveedores ORDER BY nombre";
        List<Proveedor> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Inserta un proveedor y devuelve su id generado.
     */
    public int insert(Connection conn, Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedores (nombre, contacto, telefono, direccion, usuario_creacion, fecha_creacion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getContacto());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            JdbcUtil.setInteger(ps, 5, p.getUsuarioCreacion());
            JdbcUtil.setLocalDateTime(ps, 6, p.getFechaCreacion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * Actualiza los datos de un proveedor existente.
     */
    public int update(Connection conn, Proveedor p) throws SQLException {
        String sql = "UPDATE proveedores SET nombre = ?, contacto = ?, telefono = ?, direccion = ?, "
                + "usuario_modificacion = ?, fecha_modificacion = ? WHERE id_proveedor = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getContacto());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            JdbcUtil.setInteger(ps, 5, p.getUsuarioModificacion());
            JdbcUtil.setLocalDateTime(ps, 6, p.getFechaModificacion());
            ps.setInt(7, p.getIdProveedor());
            return ps.executeUpdate();
        }
    }

    /**
     * Elimina un proveedor por su identificador.
     */
    public int delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private static Proveedor map(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        p.setContacto(rs.getString("contacto"));
        p.setTelefono(rs.getString("telefono"));
        p.setDireccion(rs.getString("direccion"));
        p.setUsuarioCreacion(JdbcUtil.getInteger(rs, "usuario_creacion"));
        p.setFechaCreacion(JdbcUtil.getLocalDateTime(rs, "fecha_creacion"));
        p.setUsuarioModificacion(JdbcUtil.getInteger(rs, "usuario_modificacion"));
        p.setFechaModificacion(JdbcUtil.getLocalDateTime(rs, "fecha_modificacion"));
        return p;
    }
}
