package com.lacanasta.util;

import com.lacanasta.controller.CajaController;
import com.lacanasta.controller.CompraController;
import com.lacanasta.controller.ConfigController;
import com.lacanasta.controller.LoginController;
import com.lacanasta.controller.MainController;
import com.lacanasta.controller.ProductoController;
import com.lacanasta.controller.ProductoFormController;
import com.lacanasta.controller.ProveedorController;
import com.lacanasta.controller.ReporteController;
import com.lacanasta.controller.UsuarioController;
import com.lacanasta.controller.VentaController;
import com.lacanasta.service.CajaService;
import com.lacanasta.service.CategoriaService;
import com.lacanasta.service.CompraService;
import com.lacanasta.service.ConfigService;
import com.lacanasta.service.ProductoService;
import com.lacanasta.service.ProveedorService;
import com.lacanasta.service.UsuarioService;
import com.lacanasta.service.VentaService;
import javafx.util.Callback;

/**
 * Fábrica de controllers que inyecta las dependencias (servicios) al cargar
 * cada vista FXML mediante {@code FXMLLoader.setControllerFactory(...)}.
 */
public class ControllerFactory implements Callback<Class<?>, Object> {

    private final UsuarioService usuarioService = new UsuarioService();
    private final ProductoService productoService = new ProductoService();
    private final CategoriaService categoriaService = new CategoriaService();
    private final VentaService ventaService = new VentaService();
    private final CajaService cajaService = new CajaService();
    private final ConfigService configService = new ConfigService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final CompraService compraService = new CompraService();

    /**
     * Crea el controller solicitado con sus dependencias inyectadas.
     *
     * @param type tipo del controller.
     * @return la instancia del controller.
     */
    @Override
    public Object call(Class<?> type) {
        if (type == LoginController.class) {
            return new LoginController(usuarioService);
        }
        if (type == MainController.class) {
            return new MainController();
        }
        if (type == VentaController.class) {
            return new VentaController(productoService, ventaService, cajaService, configService);
        }
        if (type == ProductoController.class) {
            return new ProductoController(productoService, categoriaService);
        }
        if (type == ProductoFormController.class) {
            return new ProductoFormController(productoService, categoriaService);
        }
        if (type == CajaController.class) {
            return new CajaController(cajaService);
        }
        if (type == ReporteController.class) {
            return new ReporteController(ventaService, productoService);
        }
        if (type == CompraController.class) {
            return new CompraController(compraService, proveedorService, productoService);
        }
        if (type == ProveedorController.class) {
            return new ProveedorController(proveedorService);
        }
        if (type == UsuarioController.class) {
            return new UsuarioController(usuarioService);
        }
        if (type == ConfigController.class) {
            return new ConfigController(configService);
        }
        throw new IllegalStateException("Controller no registrado: " + type.getName());
    }
}
