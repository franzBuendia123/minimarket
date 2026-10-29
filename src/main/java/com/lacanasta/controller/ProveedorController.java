package com.lacanasta.controller;

import com.lacanasta.model.Proveedor;
import com.lacanasta.service.ProveedorService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador del módulo de proveedores (CRUD).
 */
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    private TableView<Proveedor> tabla;

    @FXML
    private TableColumn<Proveedor, String> colNombre;

    @FXML
    private TableColumn<Proveedor, String> colContacto;

    @FXML
    private TableColumn<Proveedor, String> colTelefono;

    @FXML
    private TableColumn<Proveedor, String> colDireccion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtContacto;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtDireccion;

    private final ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
    private Proveedor proveedorActual;

    /**
     * Inicializa la tabla y carga los proveedores.
     */
    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colContacto.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getContacto() == null ? "" : cell.getValue().getContacto()));
        colTelefono.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getTelefono() == null ? "" : cell.getValue().getTelefono()));
        colDireccion.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getDireccion() == null ? "" : cell.getValue().getDireccion()));
        tabla.setItems(proveedores);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            if (sel != null) {
                cargarFormulario(sel);
            }
        });

        cargarProveedores();
    }

    private void cargarProveedores() {
        try {
            proveedores.setAll(proveedorService.findAll());
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    private void cargarFormulario(Proveedor p) {
        proveedorActual = p;
        txtNombre.setText(p.getNombre());
        txtContacto.setText(p.getContacto());
        txtTelefono.setText(p.getTelefono());
        txtDireccion.setText(p.getDireccion());
    }

    /**
     * Limpia el formulario para registrar un nuevo proveedor.
     */
    @FXML
    private void onNuevo() {
        proveedorActual = null;
        txtNombre.clear();
        txtContacto.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        tabla.getSelectionModel().clearSelection();
    }

    /**
     * Guarda el proveedor (crea o actualiza).
     */
    @FXML
    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Proveedores", "El nombre es obligatorio");
            return;
        }
        try {
            if (proveedorActual == null) {
                proveedorActual = new Proveedor();
            }
            proveedorActual.setNombre(nombre);
            proveedorActual.setContacto(txtContacto.getText().trim());
            proveedorActual.setTelefono(txtTelefono.getText().trim());
            proveedorActual.setDireccion(txtDireccion.getText().trim());
            if (proveedorActual.getUsuarioCreacion() == null && SessionManager.getUsuarioActual() != null) {
                proveedorActual.setUsuarioCreacion(SessionManager.getUsuarioActual().getIdUsuario());
            }
            proveedorService.save(proveedorActual);
            AlertUtil.mostrarInfo("Proveedores", "Proveedor guardado");
            cargarProveedores();
            onNuevo();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Elimina el proveedor seleccionado.
     */
    @FXML
    private void onEliminar() {
        if (proveedorActual == null || proveedorActual.getIdProveedor() == 0) {
            AlertUtil.mostrarAdvertencia("Proveedores", "Seleccione un proveedor");
            return;
        }
        if (!AlertUtil.mostrarConfirmacion("Proveedores", "¿Eliminar este proveedor?")) {
            return;
        }
        try {
            proveedorService.delete(proveedorActual.getIdProveedor());
            cargarProveedores();
            onNuevo();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }
}
