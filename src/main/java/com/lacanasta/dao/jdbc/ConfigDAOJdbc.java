package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.ConfigDAO;

import com.lacanasta.model.EmpresaConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos de la tabla {@code empresa_configuracion} (singleton, id_config = 1).
 */
public class ConfigDAOJdbc implements ConfigDAO {

    private static final String COLUMNS =
            "id_config, nombre_comercial, ruc, direccion, telefono, mensaje_ticket";

    /**
     * Obtiene la configuración única de la empresa.
     */
    public EmpresaConfig find(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM empresa_configuracion WHERE id_config = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? map(rs) : null;
        }
    }

    /**
     * Actualiza la configuración de la empresa.
     */
    public int update(Connection conn, EmpresaConfig e) throws SQLException {
        String sql = "UPDATE empresa_configuracion SET nombre_comercial = ?, ruc = ?, direccion = ?, telefono = ?, "
                + "mensaje_ticket = ? WHERE id_config = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombreComercial());
            ps.setString(2, e.getRuc());
            ps.setString(3, e.getDireccion());
            ps.setString(4, e.getTelefono());
            ps.setString(5, e.getMensajeTicket());
            return ps.executeUpdate();
        }
    }

    private static EmpresaConfig map(ResultSet rs) throws SQLException {
        EmpresaConfig e = new EmpresaConfig();
        e.setIdConfig(rs.getInt("id_config"));
        e.setNombreComercial(rs.getString("nombre_comercial"));
        e.setRuc(rs.getString("ruc"));
        e.setDireccion(rs.getString("direccion"));
        e.setTelefono(rs.getString("telefono"));
        e.setMensajeTicket(rs.getString("mensaje_ticket"));
        return e;
    }
}
