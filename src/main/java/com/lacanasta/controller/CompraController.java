package com.lacanasta.controller;

import com.lacanasta.model.DetalleCompra;
import com.lacanasta.model.Producto;
import com.lacanasta.model.Proveedor;
import com.lacanasta.service.CompraService;
import com.lacanasta.service.ProductoService;
import com.lacanasta.service.ProveedorService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del módulo de compras (registro con actualización de stock y kardex).
 */
public class CompraController {

    private final CompraService compraService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;

    public CompraController(CompraService compraService, ProveedorService proveedorService,
                            ProductoService productoService) {
        this.compraService = compraService;
        this.proveedorService = proveedorService;
        this.productoService = productoService;
    }

    @FXML
    private ComboBox<Proveedor> cmbProveedor;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, String> colNombreProd;

    @FXML
    private TableColumn<Producto, String> colStockProd;

    @FXML
    private TableColumn<Producto, String> colPrecioProd;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtCosto;

    @FXML
    private TableView<DetalleCompra> tablaDetalle;

    @FXML
    private TableColumn<DetalleCompra, String> colProductoDetalle;

    @FXML
    private TableColumn<DetalleCompra, String> colCantidadDetalle;

    @FXML
    private TableColumn<DetalleCompra, String> colCostoDetalle;

    @FXML
    private TableColumn<DetalleCompra, String> colSubtotalDetalle;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final ObservableList<DetalleCompra> detalle = FXCollections.observableArrayList();
    private final Map<Integer, String> nombresProductos = new HashMap<>();

    /**
     * Inicializa las tablas y carga los datos.
     */
    @FXML
    private void initialize() {
        cmbProveedor.setItems(FXCollections.observableArrayList(proveedorService.findAll()));

        colNombreProd.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colStockProd.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getStock())));
        colPrecioProd.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPrecioVenta().toPlainString()));
        tablaProductos.setItems(productos);

        colProductoDetalle.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                nombresProductos.getOrDefault(cell.getValue().getIdProducto(), "")));
        colCantidadDetalle.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getCantidad())));
        colCostoDetalle.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCostoUnitario().toPlainString()));
        colSubtotalDetalle.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getSubtotal().toPlainString()));
        tablaDetalle.setItems(detalle);

        cargarProductos();
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoService.findAll());
            nombresProductos.clear();
            for (Producto p : productos) {
                nombresProductos.put(p.getIdProducto(), p.getNombre());
            }
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Agrega una línea al detalle de la compra.
     */
    @FXML
    private void onAgregarLinea() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Compras", "Seleccione un producto");
            return;
        }
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            BigDecimal costo = new BigDecimal(txtCosto.getText().trim());
            if (cantidad <= 0) {
                AlertUtil.mostrarAdvertencia("Compras", "Cantidad inválida");
                return;
            }

            DetalleCompra d = new DetalleCompra();
            d.setIdProducto(seleccionado.getIdProducto());
            d.setCantidad(cantidad);
            d.setCostoUnitario(costo);
            d.setSubtotal(costo.multiply(BigDecimal.valueOf(cantidad)));
            detalle.add(d);
            actualizarTotal();
        } catch (NumberFormatException e) {
            AlertUtil.mostrarAdvertencia("Compras", "Ingrese valores numéricos válidos");
        }
    }

    private void actualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompra d : detalle) {
            total = total.add(d.getSubtotal());
        }
        lblTotal.setText("Total: S/ " + total.toPlainString());
    }

    /**
     * Registra la compra y actualiza stock y kardex.
     */
    @FXML
    private void onRegistrarCompra() {
        Proveedor proveedor = cmbProveedor.getValue();
        if (proveedor == null) {
            AlertUtil.mostrarAdvertencia("Compras", "Seleccione un proveedor");
            return;
        }
        if (detalle.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Compras", "Agregue al menos una línea al detalle");
            return;
        }
        try {
            List<DetalleCompra> lineas = new ArrayList<>(detalle);
            Integer idUsuario = SessionManager.getUsuarioActual() != null
                    ? SessionManager.getUsuarioActual().getIdUsuario() : null;
            int idCompra = compraService.registrarCompra(proveedor.getIdProveedor(), idUsuario, lineas);
            AlertUtil.mostrarInfo("Compras", "Compra #" + idCompra + " registrada correctamente");
            detalle.clear();
            actualizarTotal();
            cargarProductos();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }
}
