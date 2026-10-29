package com.lacanasta.controller;

import com.lacanasta.model.Rol;
import com.lacanasta.model.Usuario;
import com.lacanasta.service.UsuarioService;
import com.lacanasta.util.AlertUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador del módulo de usuarios (CRUD, solo ADMIN).
 */
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    private TableView<Usuario> tabla;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colUsuario;

    @FXML
    private TableColumn<Usuario, String> colRol;

    @FXML
    private TableColumn<Usuario, String> colEstado;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private ComboBox<Rol> cmbRol;

    @FXML
    private CheckBox chkEstado;

    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private Usuario usuarioActual;

    /**
     * Inicializa la tabla y el combo de roles.
     */
    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colUsuario.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getUsuario()));
        colRol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getRol())));
        colEstado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().isEstado() ? "Activo" : "Inactivo"));
        tabla.setItems(usuarios);

        cmbRol.setItems(FXCollections.observableArrayList(Rol.values()));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            if (sel != null) {
                cargarFormulario(sel);
            }
        });

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try {
            usuarios.setAll(usuarioService.findAll());
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    private void cargarFormulario(Usuario u) {
        usuarioActual = u;
        txtNombre.setText(u.getNombre());
        txtUsuario.setText(u.getUsuario());
        txtContrasena.clear();
        cmbRol.getSelectionModel().select(u.getRol());
        chkEstado.setSelected(u.isEstado());
    }

    /**
     * Limpia el formulario para registrar un nuevo usuario.
     */
    @FXML
    private void onNuevo() {
        usuarioActual = null;
        txtNombre.clear();
        txtUsuario.clear();
        txtContrasena.clear();
        cmbRol.getSelectionModel().select(Rol.CAJERO);
        chkEstado.setSelected(true);
        tabla.getSelectionModel().clearSelection();
    }

    /**
     * Guarda el usuario (crea o actualiza).
     */
    @FXML
    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (nombre.isEmpty() || usuario.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Usuarios", "Nombre y usuario son obligatorios");
            return;
        }

        try {
            if (usuarioActual == null) {
                if (contrasena.isEmpty()) {
                    AlertUtil.mostrarAdvertencia("Usuarios", "La contraseña es obligatoria para un usuario nuevo");
                    return;
                }
                usuarioActual = new Usuario();
                usuarioActual.setContrasena(contrasena);
            } else if (!contrasena.isEmpty()) {
                usuarioActual.setContrasena(contrasena);
            }

            usuarioActual.setNombre(nombre);
            usuarioActual.setUsuario(usuario);
            usuarioActual.setRol(cmbRol.getValue() == null ? Rol.CAJERO : cmbRol.getValue());
            usuarioActual.setEstado(chkEstado.isSelected());

            usuarioService.save(usuarioActual);
            AlertUtil.mostrarInfo("Usuarios", "Usuario guardado");
            cargarUsuarios();
            onNuevo();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Elimina el usuario seleccionado.
     */
    @FXML
    private void onEliminar() {
        if (usuarioActual == null || usuarioActual.getIdUsuario() == 0) {
            AlertUtil.mostrarAdvertencia("Usuarios", "Seleccione un usuario");
            return;
        }
        if (!AlertUtil.mostrarConfirmacion("Usuarios", "¿Eliminar este usuario?")) {
            return;
        }
        try {
            usuarioService.delete(usuarioActual.getIdUsuario());
            cargarUsuarios();
            onNuevo();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }
}
