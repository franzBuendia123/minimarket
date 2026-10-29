package com.lacanasta.controller;

import com.lacanasta.util.ViewLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integración de UI del controlador {@link LoginController} con TestFX.
 *
 * <p>Se prueban las validaciones de formulario que NO requieren acceso a la base
 * de datos (campos vacíos).</p>
 */
@ExtendWith(ApplicationExtension.class)
class LoginControllerTest {

    @Start
    void start(Stage stage) throws Exception {
        Parent root = ViewLoader.load("/com/lacanasta/fxml/login.fxml");
        stage.setScene(new Scene(root, 420, 520));
        stage.show();
    }

    @Test
    void loginConCamposVaciosDebeMostrarError(FxRobot robot) {
        robot.clickOn("Ingresar");

        Label lblError = robot.lookup("#lblError").queryAs(Label.class);
        assertTrue(lblError.isVisible());
        assertEquals("Ingrese usuario y contraseña", lblError.getText());
    }

    @Test
    void loginConSoloUsuarioDebeMostrarError(FxRobot robot) {
        robot.clickOn("#txtUsuario").write("admin");
        robot.clickOn("Ingresar");

        Label lblError = robot.lookup("#lblError").queryAs(Label.class);
        assertTrue(lblError.isVisible());
        assertEquals("Ingrese usuario y contraseña", lblError.getText());
    }
}
