package com.lacanasta.controller;

import com.lacanasta.model.Categoria;
import com.lacanasta.model.Producto;
import com.lacanasta.service.CategoriaService;
import com.lacanasta.service.ProductoService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.ViewLoader;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador del módulo de gestión de productos (listado y acciones).
 */
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @FXML
    private TableView<Producto> tabla;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private TableColumn<Producto, String> colPrecioNormal;

    @FXML
    private TableColumn<Producto, String> colPrecioDescuento;

    @FXML
    private TableColumn<Producto, String> colStock;

    @FXML
    private TableColumn<Producto, String> colEstado;

    @FXML
    private TextField txtBuscar;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final Map<Integer, String> nombresCategorias = new HashMap<>();

    /**
     * Inicializa la tabla y carga los productos.
     */
    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getCodigoBarras() == null ? "" : cell.getValue().getCodigoBarras()));
        colNombre.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colCategoria.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                nombreCategoria(cell.getValue().getIdCategoria())));
        colPrecioNormal.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getPrecioNormal() == null ? "" : cell.getValue().getPrecioNormal().toString()));
        colPrecioDescuento.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getPrecioDescuento() == null ? "" : cell.getValue().getPrecioDescuento().toString()));
        colStock.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                String.valueOf(cell.getValue().getStock())));
        colEstado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().isEstado() ? "Activo" : "Inactivo"));

        tabla.setItems(productos);
        cargarCategorias();
        cargarProductos();
    }

    private void cargarCategorias() {
        for (Categoria c : categoriaService.findAll()) {
            nombresCategorias.put(c.getIdCategoria(), c.getNombre());
        }
    }

    private String nombreCategoria(Integer idCategoria) {
        if (idCategoria == null) {
            return "";
        }
        return nombresCategorias.getOrDefault(idCategoria, "");
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoService.findAll());
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Busca productos por nombre.
     */
    @FXML
    private void onBuscar() {
        String texto = txtBuscar.getText().trim();
        try {
            if (texto.isEmpty()) {
                cargarProductos();
            } else {
                productos.setAll(productoService.search(texto));
            }
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Abre el formulario para crear un nuevo producto.
     */
    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    /**
     * Abre el formulario para editar el producto seleccionado.
     */
    @FXML
    private void onEditar() {
        Producto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Productos", "Seleccione un producto para editar");
            return;
        }
        abrirFormulario(seleccionado);
    }

    /**
     * Activa o desactiva el producto seleccionado.
     */
    @FXML
    private void onActivarDesactivar() {
        Producto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Productos", "Seleccione un producto");
            return;
        }
        seleccionado.setEstado(!seleccionado.isEstado());
        try {
            productoService.save(seleccionado);
            cargarProductos();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    private void abrirFormulario(Producto producto) {
        try {
            FXMLLoader loader = ViewLoader.loader("/com/lacanasta/fxml/producto-form.fxml");
            Parent root = loader.load();
            ProductoFormController controller = loader.getController();
            controller.initData(producto);

            Stage stage = new Stage();
            stage.setTitle(producto == null ? "Nuevo Producto" : "Editar Producto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarProductos();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }
}
