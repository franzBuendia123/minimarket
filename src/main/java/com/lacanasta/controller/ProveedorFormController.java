package com.lacanasta.controller;

import com.lacanasta.model.Proveedor;
import com.lacanasta.service.ProveedorService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador del formulario de creación/edición de proveedores.
 */
public class ProveedorFormController {

    private final ProveedorService proveedorService;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtContacto;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtDireccion;

    private Proveedor proveedor;

    public ProveedorFormController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    /**
     * Prepara el formulario: vacío para un proveedor nuevo o cargado para edición.
     */
    public void initData(Proveedor proveedor) {
        this.proveedor = proveedor;
        if (proveedor != null) {
            txtNombre.setText(proveedor.getNombre());
            txtContacto.setText(proveedor.getContacto());
            txtTelefono.setText(proveedor.getTelefono());
            txtDireccion.setText(proveedor.getDireccion());
        }
    }

    @FXML
    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Proveedores", "El nombre es obligatorio");
            return;
        }
        try {
            if (proveedor == null) {
                proveedor = new Proveedor();
                proveedor.setEstado(true);
            }
            proveedor.setNombre(nombre);
            proveedor.setContacto(txtContacto.getText().trim());
            proveedor.setTelefono(txtTelefono.getText().trim());
            proveedor.setDireccion(txtDireccion.getText().trim());
            if (proveedor.getUsuarioCreacion() == null && SessionManager.getUsuarioActual() != null) {
                proveedor.setUsuarioCreacion(SessionManager.getUsuarioActual().getIdUsuario());
            }
            proveedorService.save(proveedor);
            cerrar();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}
