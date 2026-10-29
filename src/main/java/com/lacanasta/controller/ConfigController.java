package com.lacanasta.controller;

import com.lacanasta.model.EmpresaConfig;
import com.lacanasta.service.ConfigService;
import com.lacanasta.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Controlador del módulo de configuración de la empresa.
 */
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @FXML
    private TextField txtNombreComercial;

    @FXML
    private TextField txtRuc;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtMensajeTicket;

    @FXML
    private Button btnDatosNegocio;

    @FXML
    private Button btnComprobantes;

    @FXML
    private Button btnVentasCaja;

    @FXML
    private Button btnPreferencias;

    /**
     * Carga la configuración actual en el formulario.
     */
    @FXML
    private void initialize() {
        try {
            EmpresaConfig config = configService.get();
            if (config != null) {
                txtNombreComercial.setText(config.getNombreComercial());
                txtRuc.setText(config.getRuc());
                txtDireccion.setText(config.getDireccion());
                txtTelefono.setText(config.getTelefono());
                txtMensajeTicket.setText(config.getMensajeTicket());
            }
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Guarda la configuración de la empresa.
     */
    @FXML
    private void onGuardar() {
        try {
            EmpresaConfig config = configService.get();
            if (config == null) {
                config = new EmpresaConfig();
            }
            config.setNombreComercial(txtNombreComercial.getText().trim());
            config.setRuc(txtRuc.getText().trim());
            config.setDireccion(txtDireccion.getText().trim());
            config.setTelefono(txtTelefono.getText().trim());
            config.setMensajeTicket(txtMensajeTicket.getText().trim());
            configService.update(config);
            AlertUtil.mostrarInfo("Configuración", "Configuración guardada correctamente");
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    @FXML
    private void onDatosNegocio() {
        activarSeccion(btnDatosNegocio);
    }

    @FXML
    private void onComprobantes() {
        activarSeccion(btnComprobantes);
    }

    @FXML
    private void onVentasCaja() {
        activarSeccion(btnVentasCaja);
    }

    @FXML
    private void onPreferencias() {
        activarSeccion(btnPreferencias);
    }

    private void activarSeccion(Button activo) {
        for (Button b : List.of(btnDatosNegocio, btnComprobantes, btnVentasCaja, btnPreferencias)) {
            b.getStyleClass().remove("active");
        }
        activo.getStyleClass().add("active");
    }
}
