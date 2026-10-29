package com.lacanasta.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Utilidad que centraliza la carga de vistas FXML aplicando la fábrica de
 * controllers para inyectar dependencias.
 */
public final class ViewLoader {

    private static final ControllerFactory CONTROLLER_FACTORY = new ControllerFactory();

    private ViewLoader() {
    }

    /**
     * Carga una vista FXML aplicando la fábrica de controllers.
     *
     * @param fxmlPath ruta absoluta del recurso FXML (classpath).
     * @return la raíz de la vista cargada.
     * @throws IOException si falla la carga.
     */
    public static Parent load(String fxmlPath) throws IOException {
        return loader(fxmlPath).load();
    }

    /**
     * Crea un {@link FXMLLoader} configurado con la fábrica de controllers.
     *
     * <p>Útil cuando además de la vista se necesita el controller (por ejemplo,
     * para pasar datos iniciales con un método {@code initData}).</p>
     *
     * @param fxmlPath ruta absoluta del recurso FXML (classpath).
     * @return el loader configurado.
     */
    public static FXMLLoader loader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(fxmlPath));
        loader.setControllerFactory(CONTROLLER_FACTORY);
        return loader;
    }
}
