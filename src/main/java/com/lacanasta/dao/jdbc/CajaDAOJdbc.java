package com.lacanasta.dao.jdbc;

import com.lacanasta.dao.CajaDAO;

import com.lacanasta.model.Caja;
import com.lacanasta.model.EstadoCaja;
import com.lacanasta.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code caja}.
 */
public class CajaDAOJdbc implements CajaDAO {

    private static final String COLUMNS =
            "id_caja, fecha_apertura, fecha_cierre, monto_inicial, monto_final, monto_esperado, "
            + "diferencia, usuario_apertura, usuario_cierre, estado";

    /**
     * Busca una caja por su identificador.
     */
    public Caja findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM caja WHERE id_caja = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Busca la caja que se encuentra abierta, si existe.
     */
    public Caja findCajaAbierta(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM caja WHERE estado = 'ABIERTO' ORDER BY id_caja DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? map(rs) : null;
        }
    }

    /**
     * Lista todas las cajas, de la más reciente a la más antigua.
     */
    public List<Caja> findAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM caja ORDER BY id_caja DESC";
        List<Caja> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Inserta una nueva caja y devuelve su id generado.
     */
    public int insert(Connection conn, Caja c) throws SQLException {
        String sql = "INSERT INTO caja (fecha_apertura, fecha_cierre, monto_inicial, monto_final, monto_esperado, "
                + "diferencia, usuario_apertura, usuario_cierre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            JdbcUtil.setLocalDateTime(ps, 1, c.getFechaApertura());
            JdbcUtil.setLocalDateTime(ps, 2, c.getFechaCierre());
            ps.setBigDecimal(3, c.getMontoInicial());
            ps.setBigDecimal(4, c.getMontoFinal());
            ps.setBigDecimal(5, c.getMontoEsperado());
            ps.setBigDecimal(6, c.getDiferencia());
            ps.setInt(7, c.getUsuarioApertura());
            JdbcUtil.setInteger(ps, 8, c.getUsuarioCierre());
            ps.setString(9, c.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * Actualiza los datos de cierre de una caja existente.
     */
    public int update(Connection conn, Caja c) throws SQLException {
        String sql = "UPDATE caja SET fecha_cierre = ?, monto_final = ?, monto_esperado = ?, diferencia = ?, "
                + "usuario_cierre = ?, estado = ? WHERE id_caja = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtil.setLocalDateTime(ps, 1, c.getFechaCierre());
            ps.setBigDecimal(2, c.getMontoFinal());
            ps.setBigDecimal(3, c.getMontoEsperado());
            ps.setBigDecimal(4, c.getDiferencia());
            JdbcUtil.setInteger(ps, 5, c.getUsuarioCierre());
            ps.setString(6, c.getEstado().name());
            ps.setInt(7, c.getIdCaja());
            return ps.executeUpdate();
        }
    }

    private static Caja map(ResultSet rs) throws SQLException {
        Caja c = new Caja();
        c.setIdCaja(rs.getInt("id_caja"));
        c.setFechaApertura(JdbcUtil.getLocalDateTime(rs, "fecha_apertura"));
        c.setFechaCierre(JdbcUtil.getLocalDateTime(rs, "fecha_cierre"));
        c.setMontoInicial(rs.getBigDecimal("monto_inicial"));
        c.setMontoFinal(rs.getBigDecimal("monto_final"));
        c.setMontoEsperado(rs.getBigDecimal("monto_esperado"));
        c.setDiferencia(rs.getBigDecimal("diferencia"));
        c.setUsuarioApertura(rs.getInt("usuario_apertura"));
        c.setUsuarioCierre(JdbcUtil.getInteger(rs, "usuario_cierre"));
        c.setEstado(EstadoCaja.valueOf(rs.getString("estado")));
        return c;
    }
}
