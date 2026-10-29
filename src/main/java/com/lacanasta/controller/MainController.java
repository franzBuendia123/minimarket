package com.lacanasta.controller;

import com.lacanasta.model.Rol;
import com.lacanasta.model.Usuario;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.SessionManager;
import com.lacanasta.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Controlador del dashboard principal con navegación entre módulos.
 */
public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblRol;

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnCompras;

    @FXML
    private Button btnProveedores;

    @FXML
    private Button btnUsuarios;

    @FXML
    private Button btnConfiguracion;

    /**
     * Inicializa el dashboard mostrando el usuario y aplicando el control por roles.
     */
    @FXML
    private void initialize() {
        Usuario usuario = SessionManager.getUsuarioActual();
        if (usuario != null) {
            lblUsuario.setText(usuario.getNombre());
            lblRol.setText(String.valueOf(usuario.getRol()));

            boolean admin = usuario.getRol() == Rol.ADMIN;
            ocultarSiNoAdmin(btnProductos, admin);
            ocultarSiNoAdmin(btnCompras, admin);
            ocultarSiNoAdmin(btnProveedores, admin);
            ocultarSiNoAdmin(btnUsuarios, admin);
            ocultarSiNoAdmin(btnConfiguracion, admin);
        }
    }

    private void ocultarSiNoAdmin(Button boton, boolean admin) {
        boton.setVisible(admin);
        boton.setManaged(admin);
    }

    @FXML
    private void abrirVentas() {
        cargarModulo("/com/lacanasta/fxml/ventas.fxml");
    }

    @FXML
    private void abrirCaja() {
        cargarModulo("/com/lacanasta/fxml/caja.fxml");
    }

    @FXML
    private void abrirReportes() {
        cargarModulo("/com/lacanasta/fxml/reportes.fxml");
    }

    @FXML
    private void abrirProductos() {
        cargarModulo("/com/lacanasta/fxml/productos.fxml");
    }

    @FXML
    private void abrirCompras() {
        cargarModulo("/com/lacanasta/fxml/compras.fxml");
    }

    @FXML
    private void abrirProveedores() {
        cargarModulo("/com/lacanasta/fxml/proveedores.fxml");
    }

    @FXML
    private void abrirUsuarios() {
        cargarModulo("/com/lacanasta/fxml/usuarios.fxml");
    }

    @FXML
    private void abrirConfiguracion() {
        cargarModulo("/com/lacanasta/fxml/configuracion.fxml");
    }

    @FXML
    private void cerrarSesion() {
        try {
            SessionManager.clear();
            Parent root = ViewLoader.load("/com/lacanasta/fxml/login.fxml");
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root, 400, 360);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setTitle("La Canasta - Login");
            stage.setResizable(false);
            stage.setScene(scene);
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo cerrar la sesión: " + e.getMessage());
        }
    }

    private void cargarModulo(String fxmlPath) {
        try {
            Parent view = ViewLoader.load(fxmlPath);
            rootPane.setCenter(view);
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo cargar el módulo: " + e.getMessage());
        }
    }
}
