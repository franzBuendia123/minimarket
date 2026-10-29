package com.lacanasta.controller;

import com.lacanasta.model.Usuario;
import com.lacanasta.service.UsuarioService;
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
 * Controlador del módulo de usuarios (CRUD con formulario modal).
 */
public class UsuarioController {

    private static final int PAGE_SIZE = 10;

    private final UsuarioService usuarioService;

    @FXML
    private TableView<Usuario> tabla;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colUsuario;

    @FXML
    private TableColumn<Usuario, String> colRol;

    @FXML
    private TableColumn<Usuario, String> colEstado;

    @FXML
    private TableColumn<Usuario, String> colAcciones;

    @FXML
    private Button btnAnterior;

    @FXML
    private Button btnSiguiente;

    @FXML
    private Label lblPagina;

    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private final ObservableList<Usuario> pagina = FXCollections.observableArrayList();
    private int paginaActual = 0;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colUsuario.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getUsuario()));
        colRol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getRol())));
        colEstado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().isEstado() ? "Activo" : "Inactivo"));
        colEstado.setCellFactory(col -> new TableCell<Usuario, String>() {
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
        colAcciones.setCellFactory(col -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableRow().getItem();
                    Button btnEditar = new Button("Editar");
                    btnEditar.getStyleClass().addAll("btn", "btn-outline");
                    btnEditar.setPrefWidth(75);
                    btnEditar.setMinWidth(75);
                    btnEditar.setOnAction(e -> abrirFormulario(u));
                    Button btnEstado = new Button();
                    btnEstado.setPrefWidth(100);
                    btnEstado.setMinWidth(100);
                    if (u.isEstado()) {
                        btnEstado.setText("Desactivar");
                        btnEstado.getStyleClass().addAll("btn", "btn-danger");
                    } else {
                        btnEstado.setText("Activar");
                        btnEstado.getStyleClass().addAll("btn", "btn-primary");
                    }
                    btnEstado.setOnAction(e -> cambiarEstado(u));
                    HBox box = new HBox(12, btnEditar, btnEstado);
                    box.setAlignment(Pos.CENTER);
                    box.setMinWidth(Region.USE_PREF_SIZE);
                    setGraphic(box);
                }
            }
        });
        tabla.setItems(pagina);
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try {
            usuarios.setAll(usuarioService.findAll());
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
        Usuario seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Usuarios", "Seleccione un usuario");
            return;
        }
        abrirFormulario(seleccionado);
    }

    private void cambiarEstado(Usuario u) {
        u.setEstado(!u.isEstado());
        try {
            usuarioService.save(u);
            cargarUsuarios();
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
        int hasta = Math.min(desde + PAGE_SIZE, usuarios.size());
        pagina.setAll(usuarios.subList(desde, hasta));
        lblPagina.setText("Página " + (paginaActual + 1) + " de " + totalPaginas);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil((double) usuarios.size() / PAGE_SIZE));
    }

    private void abrirFormulario(Usuario usuario) {
        try {
            FXMLLoader loader = ViewLoader.loader("/com/lacanasta/fxml/usuario-form.fxml");
            Parent root = loader.load();
            UsuarioFormController controller = loader.getController();
            controller.initData(usuario);

            Stage stage = new Stage();
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarUsuarios();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }
}
