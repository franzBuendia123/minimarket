package com.lacanasta.controller;

import com.lacanasta.model.Caja;
import com.lacanasta.model.MovimientoCaja;
import com.lacanasta.model.TipoMovimientoCaja;
import com.lacanasta.service.CajaService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controlador del módulo de caja (apertura, cierre y movimientos).
 */
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @FXML
    private Label lblEstadoCaja;

    @FXML
    private Label lblIdCaja;

    @FXML
    private Label lblMontoInicial;

    @FXML
    private Label lblFechaApertura;

    @FXML
    private Label lblResumen;

    @FXML
    private TableView<MovimientoCaja> tablaMovimientos;

    @FXML
    private TableColumn<MovimientoCaja, String> colFecha;

    @FXML
    private TableColumn<MovimientoCaja, String> colTipo;

    @FXML
    private TableColumn<MovimientoCaja, String> colMonto;

    @FXML
    private TableColumn<MovimientoCaja, String> colDescripcion;

    private final ObservableList<MovimientoCaja> movimientos = FXCollections.observableArrayList();
    private Caja cajaActual;

    /**
     * Inicializa la tabla y refresca el estado de caja.
     */
    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getFecha() == null ? "" : cell.getValue().getFecha().toString()));
        colTipo.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getTipo())));
        colMonto.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getMonto().toPlainString()));
        colDescripcion.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getDescripcion() == null ? "" : cell.getValue().getDescripcion()));
        tablaMovimientos.setItems(movimientos);
        refrescar();
    }

    private void refrescar() {
        try {
            cajaActual = cajaService.getCajaAbierta();
            if (cajaActual == null) {
                lblEstadoCaja.setText("Estado: SIN CAJA ABIERTA");
                lblIdCaja.setText("Id: -");
                lblMontoInicial.setText("Monto inicial: -");
                lblFechaApertura.setText("Apertura: -");
                lblResumen.setText("");
                movimientos.clear();
            } else {
                lblEstadoCaja.setText("Estado: " + cajaActual.getEstado());
                lblIdCaja.setText("Id: " + cajaActual.getIdCaja());
                lblMontoInicial.setText("Monto inicial: S/ " + cajaActual.getMontoInicial().toPlainString());
                lblFechaApertura.setText("Apertura: " + cajaActual.getFechaApertura());
                movimientos.setAll(cajaService.getMovimientos(cajaActual.getIdCaja()));
                actualizarResumen();
            }
        } catch (Exception e) {
            AlertUtil.mostrarError("Caja", e.getMessage());
        }
    }

    private void actualizarResumen() {
        BigDecimal saldo = cajaActual.getMontoInicial() == null ? BigDecimal.ZERO : cajaActual.getMontoInicial();
        for (MovimientoCaja m : movimientos) {
            if (m.getTipo() == TipoMovimientoCaja.INGRESO) {
                saldo = saldo.add(m.getMonto());
            } else {
                saldo = saldo.subtract(m.getMonto());
            }
        }
        lblResumen.setText("Saldo actual (inicial + movimientos): S/ " + saldo.toPlainString());
    }

    /**
     * Abre un nuevo turno de caja solicitando el monto inicial.
     */
    @FXML
    private void onAbrirCaja() {
        if (cajaActual != null) {
            AlertUtil.mostrarAdvertencia("Caja", "Ya existe una caja abierta");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Abrir caja");
        dialog.setHeaderText(null);
        dialog.setContentText("Monto inicial:");
        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            try {
                BigDecimal monto = new BigDecimal(resultado.get());
                cajaService.abrirCaja(monto, SessionManager.getUsuarioActual().getIdUsuario());
                AlertUtil.mostrarInfo("Caja", "Caja abierta correctamente");
                refrescar();
            } catch (NumberFormatException e) {
                AlertUtil.mostrarError("Caja", "Monto inválido");
            } catch (Exception e) {
                AlertUtil.mostrarError("Caja", e.getMessage());
            }
        }
    }

    /**
     * Cierra el turno de caja actual solicitando el monto final declarado.
     */
    @FXML
    private void onCerrarCaja() {
        if (cajaActual == null) {
            AlertUtil.mostrarAdvertencia("Caja", "No hay caja abierta");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Cerrar caja");
        dialog.setHeaderText(null);
        dialog.setContentText("Monto final (declarado):");
        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            try {
                BigDecimal montoFinal = new BigDecimal(resultado.get());
                cajaService.cerrarCaja(montoFinal, SessionManager.getUsuarioActual().getIdUsuario());
                AlertUtil.mostrarInfo("Caja", "Caja cerrada correctamente");
                refrescar();
            } catch (NumberFormatException e) {
                AlertUtil.mostrarError("Caja", "Monto inválido");
            } catch (Exception e) {
                AlertUtil.mostrarError("Caja", e.getMessage());
            }
        }
    }

    /**
     * Registra un ingreso o egreso extraordinario.
     */
    @FXML
    private void onRegistrarMovimiento() {
        if (cajaActual == null) {
            AlertUtil.mostrarAdvertencia("Caja", "No hay caja abierta");
            return;
        }
        ChoiceDialog<TipoMovimientoCaja> tipoDialog = new ChoiceDialog<>(TipoMovimientoCaja.INGRESO, TipoMovimientoCaja.values());
        tipoDialog.setTitle("Movimiento");
        tipoDialog.setHeaderText(null);
        tipoDialog.setContentText("Tipo:");
        Optional<TipoMovimientoCaja> tipo = tipoDialog.showAndWait();
        if (tipo.isEmpty()) {
            return;
        }

        TextInputDialog montoDialog = new TextInputDialog("0.00");
        montoDialog.setTitle("Movimiento");
        montoDialog.setHeaderText(null);
        montoDialog.setContentText("Monto:");
        Optional<String> monto = montoDialog.showAndWait();
        if (monto.isEmpty()) {
            return;
        }

        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setTitle("Movimiento");
        descDialog.setHeaderText(null);
        descDialog.setContentText("Descripción:");
        Optional<String> desc = descDialog.showAndWait();

        try {
            MovimientoCaja m = new MovimientoCaja();
            m.setIdCaja(cajaActual.getIdCaja());
            m.setTipo(tipo.get());
            m.setMonto(new BigDecimal(monto.get()));
            m.setDescripcion(desc.orElse(null));
            m.setIdUsuarioRegistro(SessionManager.getUsuarioActual().getIdUsuario());
            cajaService.registrarMovimiento(m);
            refrescar();
        } catch (NumberFormatException e) {
            AlertUtil.mostrarError("Caja", "Monto inválido");
        } catch (Exception e) {
            AlertUtil.mostrarError("Caja", e.getMessage());
        }
    }
}
