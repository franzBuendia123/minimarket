package com.lacanasta.service;

import com.lacanasta.dao.CajaDAO;
import com.lacanasta.dao.MovimientoCajaDAO;
import com.lacanasta.dao.VentaDAO;
import com.lacanasta.model.Caja;
import com.lacanasta.model.EstadoCaja;
import com.lacanasta.model.MovimientoCaja;
import com.lacanasta.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para la apertura, cierre y movimientos de caja.
 */
public class CajaService {

    private final CajaDAO cajaDAO;
    private final MovimientoCajaDAO movimientoCajaDAO;
    private final VentaDAO ventaDAO;

    /**
     * Constructor por defecto que crea las dependencias reales.
     */
    public CajaService() {
        this(new CajaDAO(), new MovimientoCajaDAO(), new VentaDAO());
    }

    /**
     * Constructor para inyección de dependencias (pruebas).
     */
    public CajaService(CajaDAO cajaDAO, MovimientoCajaDAO movimientoCajaDAO, VentaDAO ventaDAO) {
        this.cajaDAO = cajaDAO;
        this.movimientoCajaDAO = movimientoCajaDAO;
        this.ventaDAO = ventaDAO;
    }

    /**
     * Abre un nuevo turno de caja.
     *
     * @param montoInicial monto con el que inicia el turno.
     * @param idUsuario    id del usuario que abre la caja.
     * @return la caja creada.
     */
    public Caja abrirCaja(BigDecimal montoInicial, int idUsuario) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Caja abierta = cajaDAO.findCajaAbierta(conn);
            if (abierta != null) {
                throw new ServiceException("Ya existe una caja abierta");
            }
            Caja caja = new Caja();
            caja.setFechaApertura(LocalDateTime.now());
            caja.setMontoInicial(montoInicial);
            caja.setUsuarioApertura(idUsuario);
            caja.setEstado(EstadoCaja.ABIERTO);
            int id = cajaDAO.insert(conn, caja);
            caja.setIdCaja(id);
            return caja;
        } catch (SQLException e) {
            throw new ServiceException("Error al abrir caja", e);
        }
    }

    /**
     * Devuelve la caja abierta actual, o {@code null} si no hay ninguna.
     */
    public Caja getCajaAbierta() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return cajaDAO.findCajaAbierta(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar caja abierta", e);
        }
    }

    /**
     * Lista todas las cajas.
     */
    public List<Caja> findAll() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return cajaDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al listar cajas", e);
        }
    }

    /**
     * Registra un movimiento de caja (ingreso o egreso extraordinario).
     */
    public void registrarMovimiento(MovimientoCaja movimiento) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (movimiento.getFecha() == null) {
                movimiento.setFecha(LocalDateTime.now());
            }
            movimientoCajaDAO.insert(conn, movimiento);
        } catch (SQLException e) {
            throw new ServiceException("Error al registrar el movimiento de caja", e);
        }
    }

    /**
     * Lista los movimientos de una caja.
     */
    public List<MovimientoCaja> getMovimientos(int idCaja) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return movimientoCajaDAO.findByCaja(conn, idCaja);
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar movimientos de caja", e);
        }
    }

    /**
     * Cierra el turno de caja actual calculando el monto esperado y la diferencia.
     *
     * <p>El monto esperado se calcula como {@code monto_inicial + SUM(total_ventas)}.
     * La diferencia es {@code monto_final - monto_esperado}.</p>
     *
     * @param montoFinal      monto final declarado por el usuario.
     * @param idUsuarioCierre id del usuario que cierra la caja.
     */
    public void cerrarCaja(BigDecimal montoFinal, int idUsuarioCierre) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Caja caja = cajaDAO.findCajaAbierta(conn);
                if (caja == null) {
                    throw new ServiceException("No hay caja abierta para cerrar");
                }
                BigDecimal totalVentas = ventaDAO.sumTotalByCaja(conn, caja.getIdCaja());
                BigDecimal montoInicial = caja.getMontoInicial() == null ? BigDecimal.ZERO : caja.getMontoInicial();
                BigDecimal montoEsperado = montoInicial.add(totalVentas);
                BigDecimal diferencia = montoFinal.subtract(montoEsperado);

                caja.setMontoFinal(montoFinal);
                caja.setMontoEsperado(montoEsperado);
                caja.setDiferencia(diferencia);
                caja.setFechaCierre(LocalDateTime.now());
                caja.setUsuarioCierre(idUsuarioCierre);
                caja.setEstado(EstadoCaja.CERRADO);
                cajaDAO.update(conn, caja);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof ServiceException) {
                    throw (ServiceException) e;
                }
                throw new ServiceException("Error al cerrar caja", e);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error de conexión al cerrar caja", e);
        }
    }
}
