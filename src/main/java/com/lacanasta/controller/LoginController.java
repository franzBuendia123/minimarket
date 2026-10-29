package com.lacanasta.controller;

import com.lacanasta.model.Usuario;
import com.lacanasta.service.UsuarioService;
import com.lacanasta.util.SessionManager;
import com.lacanasta.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador de la vista de login.
 */
public class LoginController {

    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblError;

    /**
     * Maneja el evento de inicio de sesión.
     */
    @FXML
    private void onLogin() {
        String username = txtUsuario.getText().trim();
        String password = txtContrasena.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Ingrese usuario y contraseña");
            return;
        }

        try {
            Usuario usuario = usuarioService.authenticate(username, password);
            if (usuario == null) {
                mostrarError("Usuario o contraseña incorrectos");
                return;
            }
            SessionManager.setUsuarioActual(usuario);
            abrirPantallaPrincipal();
        } catch (Exception e) {
            mostrarError("Error de conexión: " + e.getMessage());
        }
    }

    private void abrirPantallaPrincipal() throws Exception {
        Parent root = ViewLoader.load("/com/lacanasta/fxml/main-view.fxml");
        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setTitle("La Canasta - Sistema POS");
        stage.setResizable(true);
        stage.setScene(scene);
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
