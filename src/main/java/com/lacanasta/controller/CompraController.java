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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private TableColumn<Producto, String> colAcciones;

    @FXML
    private ListView<ItemCompra> lstDetalle;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final ObservableList<ItemCompra> detalle = FXCollections.observableArrayList();

    /**
     * Inicializa las tablas, la lista del detalle y carga los productos.
     */
    @FXML
    private void initialize() {
        cmbProveedor.setItems(FXCollections.observableArrayList(proveedorService.findAll()));

        colNombreProd.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colStockProd.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getStock())));
        colPrecioProd.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPrecioVenta().toPlainString()));
        colAcciones.setCellFactory(col -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Producto p = getTableRow().getItem();
                    Button btnAgregar = new Button("+ Agregar");
                    btnAgregar.getStyleClass().addAll("btn", "btn-soft");
                    btnAgregar.setPrefWidth(110);
                    btnAgregar.setMinWidth(110);
                    btnAgregar.setOnAction(e -> agregarAlDetalle(p));
                    HBox box = new HBox(btnAgregar);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setMinWidth(Region.USE_PREF_SIZE);
                    setGraphic(box);
                }
            }
        });
        tablaProductos.setItems(productos);

        lstDetalle.setCellFactory(list -> new ListCell<ItemCompra>() {
            @Override
            protected void updateItem(ItemCompra item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(crearFila(item));
                }
            }
        });
        lstDetalle.setItems(detalle);

        cargarProductos();
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoService.findAll());
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    private void agregarAlDetalle(Producto p) {
        for (ItemCompra item : detalle) {
            if (item.getIdProducto() == p.getIdProducto()) {
                item.setCantidad(item.getCantidad() + 1);
                lstDetalle.refresh();
                actualizarTotal();
                return;
            }
        }
        BigDecimal costo = p.getCosto() == null ? BigDecimal.ZERO : p.getCosto();
        detalle.add(new ItemCompra(p, 1, costo));
        actualizarTotal();
    }

    private void cambiarCantidad(ItemCompra item, int delta) {
        int nueva = item.getCantidad() + delta;
        if (nueva <= 0) {
            detalle.remove(item);
        } else {
            item.setCantidad(nueva);
        }
        lstDetalle.refresh();
        actualizarTotal();
    }

    private void quitarItem(ItemCompra item) {
        detalle.remove(item);
        actualizarTotal();
    }

    private void actualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCompra d : detalle) {
            total = total.add(d.getSubtotal());
        }
        lblTotal.setText("Total: S/ " + total.toPlainString());
    }

    private HBox crearFila(ItemCompra item) {
        Label nombre = new Label(item.getNombre());
        nombre.getStyleClass().add("cart-item-name");
        VBox info = new VBox(2, nombre);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button menos = new Button("−");
        menos.getStyleClass().add("small-button");
        menos.setOnAction(e -> cambiarCantidad(item, -1));
        Label cantidad = new Label(String.valueOf(item.getCantidad()));
        cantidad.getStyleClass().add("cart-item-qty");
        Button mas = new Button("+");
        mas.getStyleClass().add("small-button");
        mas.setOnAction(e -> cambiarCantidad(item, 1));

        TextField costo = new TextField(item.getCostoUnitario().toPlainString());
        costo.getStyleClass().add("field");
        costo.setPrefWidth(80);

        Label subtotal = new Label("S/ " + item.getSubtotal().toPlainString());
        subtotal.getStyleClass().add("cart-item-subtotal");

        costo.textProperty().addListener((obs, o, n) -> {
            try {
                BigDecimal c = new BigDecimal(n.trim());
                item.setCostoUnitario(c);
                subtotal.setText("S/ " + item.getSubtotal().toPlainString());
                actualizarTotal();
            } catch (NumberFormatException ex) {
                // Ignorar entrada numérica inválida.
            }
        });

        Button borrar = new Button("🗑");
        borrar.getStyleClass().addAll("btn", "btn-ghost");
        borrar.setOnAction(e -> quitarItem(item));

        HBox centro = new HBox(6, menos, cantidad, mas, costo);
        centro.setAlignment(Pos.CENTER);
        HBox derecha = new HBox(8, subtotal, borrar);
        derecha.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(10, info, centro, derecha);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
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
            List<DetalleCompra> lineas = new ArrayList<>();
            for (ItemCompra item : detalle) {
                DetalleCompra d = new DetalleCompra();
                d.setIdProducto(item.getIdProducto());
                d.setCantidad(item.getCantidad());
                d.setCostoUnitario(item.getCostoUnitario());
                d.setSubtotal(item.getSubtotal());
                lineas.add(d);
            }
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

    /**
     * Modelo interno de una línea de compra: producto + cantidad + costo unitario.
     */
    private static class ItemCompra {
        private final Producto producto;
        private int cantidad;
        private BigDecimal costoUnitario;

        ItemCompra(Producto producto, int cantidad, BigDecimal costoUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.costoUnitario = costoUnitario;
        }

        public Producto getProducto() {
            return producto;
        }

        public int getIdProducto() {
            return producto.getIdProducto();
        }

        public String getNombre() {
            return producto.getNombre();
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getCostoUnitario() {
            return costoUnitario;
        }

        public void setCostoUnitario(BigDecimal costoUnitario) {
            this.costoUnitario = costoUnitario;
        }

        public BigDecimal getSubtotal() {
            return costoUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
    }
}
