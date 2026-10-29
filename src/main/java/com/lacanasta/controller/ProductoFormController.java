package com.lacanasta.controller;

import com.lacanasta.model.Categoria;
import com.lacanasta.model.Producto;
import com.lacanasta.service.CategoriaService;
import com.lacanasta.service.ProductoService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

/**
 * Controlador del formulario de creación/edición de productos.
 */
public class ProductoFormController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoFormController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private TextField txtPrecioNormal;

    @FXML
    private TextField txtPrecioDescuento;

    @FXML
    private TextField txtStock;

    @FXML
    private CheckBox chkEstado;

    private Producto producto;

    /**
     * Inicializa el combo de categorías.
     */
    @FXML
    private void initialize() {
        cmbCategoria.setItems(FXCollections.observableArrayList(categoriaService.findAll()));
    }

    /**
     * Prepara el formulario: vacío para un producto nuevo o cargado para edición.
     *
     * @param producto el producto a editar, o {@code null} para uno nuevo.
     */
    public void initData(Producto producto) {
        this.producto = producto;
        if (producto == null) {
            chkEstado.setSelected(true);
            txtStock.setText("0");
            txtPrecioNormal.setText("0.00");
            txtPrecioDescuento.setText("0.00");
        } else {
            txtCodigo.setText(producto.getCodigoBarras());
            txtNombre.setText(producto.getNombre());
            txtPrecioNormal.setText(producto.getPrecioNormal() == null ? "0.00" : producto.getPrecioNormal().toString());
            txtPrecioDescuento.setText(producto.getPrecioDescuento() == null ? "0.00" : producto.getPrecioDescuento().toString());
            txtStock.setText(String.valueOf(producto.getStock()));
            chkEstado.setSelected(producto.isEstado());
            if (producto.getIdCategoria() != null) {
                for (Categoria c : cmbCategoria.getItems()) {
                    if (c.getIdCategoria() == producto.getIdCategoria()) {
                        cmbCategoria.getSelectionModel().select(c);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Guarda el producto (crea o actualiza) y cierra el formulario.
     */
    @FXML
    private void onGuardar() {
        try {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                AlertUtil.mostrarAdvertencia("Productos", "El nombre es obligatorio");
                return;
            }

            BigDecimal precioNormal = new BigDecimal(txtPrecioNormal.getText().trim());
            BigDecimal precioDescuento = txtPrecioDescuento.getText().trim().isEmpty()
                    ? BigDecimal.ZERO
                    : new BigDecimal(txtPrecioDescuento.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());

            if (stock < 0) {
                AlertUtil.mostrarAdvertencia("Productos", "El stock no puede ser negativo");
                return;
            }

            if (producto == null) {
                producto = new Producto();
            }
            producto.setCodigoBarras(txtCodigo.getText().trim().isEmpty() ? null : txtCodigo.getText().trim());
            producto.setNombre(nombre);
            producto.setPrecioNormal(precioNormal);
            producto.setPrecioDescuento(precioDescuento);
            producto.setStock(stock);
            producto.setEstado(chkEstado.isSelected());
            Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();
            producto.setIdCategoria(categoria == null ? null : categoria.getIdCategoria());
            if (producto.getUsuarioCreacion() == null && SessionManager.getUsuarioActual() != null) {
                producto.setUsuarioCreacion(SessionManager.getUsuarioActual().getIdUsuario());
            }

            productoService.save(producto);
            cerrar();
        } catch (NumberFormatException e) {
            AlertUtil.mostrarError("Error", "Ingrese valores numéricos válidos");
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Cancela la operación y cierra el formulario.
     */
    @FXML
    private void onCancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}
