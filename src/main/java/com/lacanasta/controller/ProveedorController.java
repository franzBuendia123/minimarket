package com.lacanasta.controller;

import com.lacanasta.model.Proveedor;
import com.lacanasta.service.ProveedorService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador del módulo de proveedores (CRUD con formulario modal).
 */
public class ProveedorController {

    private static final int PAGE_SIZE = 10;

    private final ProveedorService proveedorService;

    @FXML
    private TableView<Proveedor> tabla;

    @FXML
    private TableColumn<Proveedor, String> colNombre;

    @FXML
    private TableColumn<Proveedor, String> colContacto;

    @FXML
    private TableColumn<Proveedor, String> colTelefono;

    @FXML
    private TableColumn<Proveedor, String> colDireccion;

    @FXML
    private TableColumn<Proveedor, String> colEstado;

    @FXML
    private TableColumn<Proveedor, String> colAcciones;

    @FXML
    private Button btnAnterior;

    @FXML
    private Button btnSiguiente;

    @FXML
    private Label lblPagina;

    private final ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> pagina = FXCollections.observableArrayList();
    private int paginaActual = 0;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colContacto.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getContacto() == null ? "" : cell.getValue().getContacto()));
        colTelefono.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getTelefono() == null ? "" : cell.getValue().getTelefono()));
        colDireccion.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getDireccion() == null ? "" : cell.getValue().getDireccion()));
        colEstado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().isEstado() ? "Activo" : "Inactivo"));
        colEstado.setCellFactory(col -> new TableCell<Proveedor, String>() {
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
        colAcciones.setCellFactory(col -> new TableCell<Proveedor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Proveedor p = getTableRow().getItem();
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
        cargarProveedores();
    }

    private void cargarProveedores() {
        try {
            proveedores.setAll(proveedorService.findAll());
            paginaActual = 0;
            aplicarPagina();
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
        Proveedor seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Proveedores", "Seleccione un proveedor");
            return;
        }
        abrirFormulario(seleccionado);
    }

    private void cambiarEstado(Proveedor p) {
        p.setEstado(!p.isEstado());
        try {
            proveedorService.save(p);
            cargarProveedores();
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
        int hasta = Math.min(desde + PAGE_SIZE, proveedores.size());
        pagina.setAll(proveedores.subList(desde, hasta));
        lblPagina.setText("Página " + (paginaActual + 1) + " de " + totalPaginas);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil((double) proveedores.size() / PAGE_SIZE));
    }

    private void abrirFormulario(Proveedor proveedor) {
        try {
            FXMLLoader loader = ViewLoader.loader("/com/lacanasta/fxml/proveedor-form.fxml");
            Parent root = loader.load();
            ProveedorFormController controller = loader.getController();
            controller.initData(proveedor);

            Stage stage = new Stage();
            stage.setTitle(proveedor == null ? "Nuevo Proveedor" : "Editar Proveedor");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarProveedores();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }
}
