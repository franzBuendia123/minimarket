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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador del módulo de gestión de productos (listado, búsqueda y acciones).
 */
public class ProductoController {

    private static final int PAGE_SIZE = 10;

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
    private TableColumn<Producto, String> colAcciones;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnAnterior;

    @FXML
    private Button btnSiguiente;

    @FXML
    private Label lblPagina;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final ObservableList<Producto> pagina = FXCollections.observableArrayList();
    private final Map<Integer, String> nombresCategorias = new HashMap<>();
    private int paginaActual = 0;

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
        colEstado.setCellFactory(col -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    boolean activo = getTableRow().getItem().isEstado();
                    Label badge = new Label(activo ? "Activo" : "Inactivo");
                    badge.getStyleClass().addAll("badge", activo ? "badge-success" : "badge-danger");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Producto p = getTableRow().getItem();
                    Button btnEditar = new Button("Editar");
                    btnEditar.getStyleClass().addAll("btn", "btn-outline");
                    btnEditar.setPrefWidth(75);
                    btnEditar.setMinWidth(75);
                    btnEditar.setOnAction(e -> abrirFormulario(p));
                    Button btnEstado = new Button();
                    btnEstado.setPrefWidth(100);
                    btnEstado.setMinWidth(100);
                    if (p.isEstado()) {
                        btnEstado.setText("Desactivar");
                        btnEstado.getStyleClass().addAll("btn", "btn-danger");
                    } else {
                        btnEstado.setText("Activar");
                        btnEstado.getStyleClass().addAll("btn", "btn-primary");
                    }
                    btnEstado.setOnAction(e -> cambiarEstado(p));
                    HBox box = new HBox(12, btnEditar, btnEstado);
                    box.setAlignment(Pos.CENTER);
                    box.setMinWidth(Region.USE_PREF_SIZE);
                    setGraphic(box);
                }
            }
        });
        tabla.setItems(pagina);
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
            paginaActual = 0;
            aplicarPagina();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    @FXML
    private void onBuscar() {
        String texto = txtBuscar.getText().trim();
        try {
            if (texto.isEmpty()) {
                cargarProductos();
            } else {
                productos.setAll(productoService.search(texto));
                paginaActual = 0;
                aplicarPagina();
            }
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Producto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Productos", "Seleccione un producto para editar");
            return;
        }
        abrirFormulario(seleccionado);
    }

    private void cambiarEstado(Producto p) {
        p.setEstado(!p.isEstado());
        try {
            productoService.save(p);
            cargarProductos();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    @FXML
    private void onAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            aplicarPagina();
        }
    }

    @FXML
    private void onSiguiente() {
        int totalPaginas = totalPaginas();
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            aplicarPagina();
        }
    }

    private void aplicarPagina() {
        int totalPaginas = totalPaginas();
        if (paginaActual >= totalPaginas) {
            paginaActual = totalPaginas - 1;
        }
        int desde = paginaActual * PAGE_SIZE;
        int hasta = Math.min(desde + PAGE_SIZE, productos.size());
        pagina.setAll(productos.subList(desde, hasta));
        lblPagina.setText("Página " + (paginaActual + 1) + " de " + totalPaginas);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil((double) productos.size() / PAGE_SIZE));
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
