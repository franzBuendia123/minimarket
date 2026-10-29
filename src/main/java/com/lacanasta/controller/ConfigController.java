package com.lacanasta.controller;

import com.lacanasta.model.EmpresaConfig;
import com.lacanasta.service.ConfigService;
import com.lacanasta.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

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
}
