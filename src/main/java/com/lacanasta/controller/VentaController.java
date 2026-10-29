package com.lacanasta.controller;

import com.lacanasta.model.Caja;
import com.lacanasta.model.CarritoItem;
import com.lacanasta.model.DetalleVenta;
import com.lacanasta.model.EmpresaConfig;
import com.lacanasta.model.MetodoPago;
import com.lacanasta.model.Producto;
import com.lacanasta.model.Venta;
import com.lacanasta.service.CajaService;
import com.lacanasta.service.ConfigService;
import com.lacanasta.service.ProductoService;
import com.lacanasta.service.VentaService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.SessionManager;
import com.lacanasta.util.TicketPDFGenerator;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del módulo de ventas (carrito, búsqueda, pago y vuelto).
 */
public class VentaController {

    private final ProductoService productoService;
    private final VentaService ventaService;
    private final CajaService cajaService;
    private final ConfigService configService;

    public VentaController(ProductoService productoService, VentaService ventaService,
                           CajaService cajaService, ConfigService configService) {
        this.productoService = productoService;
        this.ventaService = ventaService;
        this.cajaService = cajaService;
        this.configService = configService;
    }

    @FXML
    private ListView<CarritoItem> lstCarrito;

    @FXML
    private TableView<Producto> tablaResultados;

    @FXML
    private TableColumn<Producto, String> colNombreResultado;

    @FXML
    private TableColumn<Producto, String> colPrecioResultado;

    @FXML
    private TableColumn<Producto, String> colStockResultado;

    @FXML
    private TableColumn<Producto, String> colAcciones;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtMontoRecibido;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblVuelto;

    @FXML
    private Label lblUnidades;

    @FXML
    private ToggleButton btnEfectivo;

    @FXML
    private ToggleButton btnTarjeta;

    @FXML
    private ToggleButton btnYape;

    @FXML
    private ToggleGroup grupoPago;

    @FXML
    private VBox panelEfectivo;

    @FXML
    private Button btnPagar;

    private final ObservableList<CarritoItem> carrito = FXCollections.observableArrayList();
    private final ObservableList<Producto> resultados = FXCollections.observableArrayList();
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    /**
     * Inicializa las tablas y los controles de pago.
     */
    @FXML
    private void initialize() {
        lstCarrito.setCellFactory(list -> new ListCell<CarritoItem>() {
            @Override
            protected void updateItem(CarritoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(crearFila(item));
                }
            }
        });
        lstCarrito.setItems(carrito);

        colNombreResultado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colPrecioResultado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPrecioVenta().toString()));
        colStockResultado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getStock())));
        colStockResultado.setCellFactory(col -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Producto p = getTableRow().getItem();
                    Label badge = new Label(p.getStock() + " unidades");
                    badge.getStyleClass().addAll("badge", p.getStock() < 5 ? "badge-warning" : "badge-neutral");
                    setGraphic(badge);
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
                    Button btnAgregar = new Button("+ Agregar");
                    btnAgregar.getStyleClass().addAll("btn", "btn-soft");
                    btnAgregar.setPrefWidth(110);
                    btnAgregar.setMinWidth(110);
                    btnAgregar.setOnAction(e -> agregarAlCarrito(p));
                    HBox box = new HBox(btnAgregar);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setMinWidth(Region.USE_PREF_SIZE);
                    setGraphic(box);
                }
            }
        });
        tablaResultados.setItems(resultados);

        txtMontoRecibido.textProperty().addListener((obs, oldVal, newVal) -> calcularVuelto());

        seleccionarMetodo(MetodoPago.EFECTIVO);
        cargarProductos();
        actualizarTotales();
    }

    private void cargarProductos() {
        try {
            resultados.setAll(productoService.findAll());
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Busca por código de barras (escáner) o por nombre.
     */
    @FXML
    private void onBuscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            cargarProductos();
            return;
        }
        try {
            Producto porCodigo = productoService.findByCodigoBarras(texto);
            if (porCodigo != null) {
                agregarAlCarrito(porCodigo);
                txtBuscar.clear();
                return;
            }
            resultados.setAll(productoService.search(texto));
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }

    /**
     * Agrega al carrito el producto seleccionado en la tabla de resultados.
     */
    @FXML
    private void onAgregar() {
        Producto seleccionado = tablaResultados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Ventas", "Seleccione un producto");
            return;
        }
        agregarAlCarrito(seleccionado);
    }

    private void agregarAlCarrito(Producto producto) {
        if (producto.getStock() < 1) {
            AlertUtil.mostrarAdvertencia("Ventas", "Sin stock para: " + producto.getNombre());
            return;
        }
        for (CarritoItem item : carrito) {
            if (item.getIdProducto() == producto.getIdProducto()) {
                if (item.getCantidad() + 1 > producto.getStock()) {
                    AlertUtil.mostrarAdvertencia("Ventas", "Stock insuficiente para: " + producto.getNombre());
                    return;
                }
                item.setCantidad(item.getCantidad() + 1);
                lstCarrito.refresh();
                actualizarTotales();
                return;
            }
        }
        carrito.add(new CarritoItem(producto, 1));
        actualizarTotales();
    }

    private void cambiarCantidad(CarritoItem item, int delta) {
        int nueva = item.getCantidad() + delta;
        if (nueva <= 0) {
            carrito.remove(item);
        } else {
            if (nueva > item.getProducto().getStock()) {
                AlertUtil.mostrarAdvertencia("Ventas", "Stock insuficiente");
                return;
            }
            item.setCantidad(nueva);
        }
        lstCarrito.refresh();
        actualizarTotales();
    }

    private void quitarItem(CarritoItem item) {
        carrito.remove(item);
        actualizarTotales();
    }

    private HBox crearFila(CarritoItem item) {
        Label nombre = new Label(item.getNombre());
        nombre.getStyleClass().add("cart-item-name");
        Label precio = new Label("S/ " + item.getPrecio().toPlainString() + " c/u");
        precio.getStyleClass().add("cart-item-price");
        VBox info = new VBox(2, nombre, precio);
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
        HBox qty = new HBox(6, menos, cantidad, mas);
        qty.setAlignment(Pos.CENTER);

        Label subtotal = new Label("S/ " + item.getSubtotal().toPlainString());
        subtotal.getStyleClass().add("cart-item-subtotal");
        Button borrar = new Button("🗑");
        borrar.getStyleClass().addAll("btn", "btn-ghost");
        borrar.setOnAction(e -> quitarItem(item));
        HBox derecha = new HBox(8, subtotal, borrar);
        derecha.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(10, info, qty, derecha);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    @FXML
    private void onVaciar() {
        carrito.clear();
        actualizarTotales();
    }

    @FXML
    private void onMetodoEfectivo() {
        seleccionarMetodo(MetodoPago.EFECTIVO);
    }

    @FXML
    private void onMetodoTarjeta() {
        seleccionarMetodo(MetodoPago.TARJETA);
    }

    @FXML
    private void onMetodoYape() {
        seleccionarMetodo(MetodoPago.YAPE);
    }

    private void seleccionarMetodo(MetodoPago metodo) {
        this.metodoPago = metodo;
        btnEfectivo.setSelected(metodo == MetodoPago.EFECTIVO);
        btnTarjeta.setSelected(metodo == MetodoPago.TARJETA);
        btnYape.setSelected(metodo == MetodoPago.YAPE);
        boolean efectivo = metodo == MetodoPago.EFECTIVO;
        panelEfectivo.setVisible(efectivo);
        panelEfectivo.setManaged(efectivo);
        actualizarTotales();
    }

    private void actualizarTotales() {
        BigDecimal total = totalCarrito();
        lblTotal.setText("S/ " + total.toPlainString());
        int unidades = carrito.stream().mapToInt(CarritoItem::getCantidad).sum();
        lblUnidades.setText(unidades + " unidades");
        btnPagar.setText("Cobrar S/ " + total.toPlainString());
        calcularVuelto();
    }

    private void calcularVuelto() {
        String texto = txtMontoRecibido.getText().trim();
        if (texto.isEmpty()) {
            lblVuelto.setText("Vuelto: S/ 0.00");
            return;
        }
        try {
            BigDecimal recibido = new BigDecimal(texto);
            BigDecimal total = totalCarrito();
            BigDecimal vuelto = recibido.subtract(total);
            if (vuelto.compareTo(BigDecimal.ZERO) < 0) {
                lblVuelto.setText("Vuelto: S/ 0.00");
            } else {
                lblVuelto.setText("Vuelto: S/ " + vuelto.toPlainString());
            }
        } catch (NumberFormatException e) {
            lblVuelto.setText("Vuelto: S/ 0.00");
        }
    }

    private BigDecimal totalCarrito() {
        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItem item : carrito) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /**
     * Registra la venta y limpia el carrito.
     */
    @FXML
    private void onPagar() {
        if (carrito.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Ventas", "El carrito está vacío");
            return;
        }

        MetodoPago metodo = metodoPago;
        BigDecimal recibido = BigDecimal.ZERO;
        if (metodo == MetodoPago.EFECTIVO) {
            try {
                recibido = new BigDecimal(txtMontoRecibido.getText().trim());
            } catch (NumberFormatException e) {
                AlertUtil.mostrarAdvertencia("Ventas", "Ingrese el monto recibido");
                return;
            }
            if (recibido.compareTo(totalCarrito()) < 0) {
                AlertUtil.mostrarAdvertencia("Ventas", "Monto recibido insuficiente");
                return;
            }
        }

        try {
            Integer idCaja = null;
            Caja cajaAbierta = cajaService.getCajaAbierta();
            if (cajaAbierta != null) {
                idCaja = cajaAbierta.getIdCaja();
            }

            List<DetalleVenta> detalles = new ArrayList<>();
            for (CarritoItem item : carrito) {
                DetalleVenta d = new DetalleVenta();
                d.setIdProducto(item.getIdProducto());
                d.setCantidad(item.getCantidad());
                detalles.add(d);
            }

            int idCajero = SessionManager.getUsuarioActual().getIdUsuario();
            int idVenta = ventaService.registrarVenta(idCajero, idCaja, metodo, detalles);

            BigDecimal vuelto = BigDecimal.ZERO;
            if (metodo == MetodoPago.EFECTIVO) {
                vuelto = recibido.subtract(totalCarrito());
            }

            try {
                Venta venta = ventaService.findById(idVenta);
                EmpresaConfig config = configService.get();
                String rutaTicket = "tickets/venta_" + idVenta + ".pdf";
                new TicketPDFGenerator().generarTicket(rutaTicket, config, venta, detalles, vuelto);
            } catch (Exception pdfEx) {
                AlertUtil.mostrarAdvertencia("Ventas", "Venta registrada, pero no se pudo generar el PDF: " + pdfEx.getMessage());
            }

            String mensaje = "Venta #" + idVenta + " registrada correctamente.";
            if (vuelto.compareTo(BigDecimal.ZERO) > 0) {
                mensaje += "\nVuelto: S/ " + vuelto.toPlainString();
            }
            AlertUtil.mostrarInfo("Ventas", mensaje);

            carrito.clear();
            txtMontoRecibido.clear();
            actualizarTotales();
            cargarProductos();
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", e.getMessage());
        }
    }
}
