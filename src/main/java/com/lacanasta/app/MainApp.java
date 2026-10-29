package com.lacanasta.app;

import javafx.application.Application;
import com.lacanasta.util.ViewLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal que lanza la aplicación JavaFX.
 */
public class MainApp extends Application {

    /**
     * Punto de entrada de la aplicación.
     *
     * @param primaryStage la ventana principal.
     * @throws Exception si falla la carga de la vista de login.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = ViewLoader.load("/com/lacanasta/fxml/login.fxml");
        Scene scene = new Scene(root, 400, 360);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("La Canasta - Login");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Método main de la aplicación.
     *
     * @param args argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
