package com.lacanasta.controller;

import com.lacanasta.model.Rol;
import com.lacanasta.model.Usuario;
import com.lacanasta.service.UsuarioService;
import com.lacanasta.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador del formulario de creación/edición de usuarios.
 */
public class UsuarioFormController {

    private final UsuarioService usuarioService;

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

    private Usuario usuario;

    public UsuarioFormController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    private void initialize() {
        cmbRol.setItems(FXCollections.observableArrayList(Rol.values()));
    }

    /**
     * Prepara el formulario: vacío para un usuario nuevo o cargado para edición.
     */
    public void initData(Usuario usuario) {
        this.usuario = usuario;
        if (usuario == null) {
            cmbRol.getSelectionModel().select(Rol.CAJERO);
            chkEstado.setSelected(true);
        } else {
            txtNombre.setText(usuario.getNombre());
            txtUsuario.setText(usuario.getUsuario());
            cmbRol.getSelectionModel().select(usuario.getRol());
            chkEstado.setSelected(usuario.isEstado());
        }
    }

    @FXML
    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        String login = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (nombre.isEmpty() || login.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Usuarios", "Nombre y usuario son obligatorios");
            return;
        }

        try {
            if (usuario == null) {
                if (contrasena.isEmpty()) {
                    AlertUtil.mostrarAdvertencia("Usuarios", "La contraseña es obligatoria para un usuario nuevo");
                    return;
                }
                usuario = new Usuario();
                usuario.setContrasena(contrasena);
            } else if (!contrasena.isEmpty()) {
                usuario.setContrasena(contrasena);
            }

            usuario.setNombre(nombre);
            usuario.setUsuario(login);
            usuario.setRol(cmbRol.getValue() == null ? Rol.CAJERO : cmbRol.getValue());
            usuario.setEstado(chkEstado.isSelected());

            usuarioService.save(usuario);
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
