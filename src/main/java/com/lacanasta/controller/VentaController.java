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
import com.lacanasta.util.TicketPDFGenerator;
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
    private TableView<CarritoItem> tablaCarrito;

    @FXML
    private TableColumn<CarritoItem, String> colProducto;

    @FXML
    private TableColumn<CarritoItem, String> colPrecio;

    @FXML
    private TableColumn<CarritoItem, String> colCantidad;

    @FXML
    private TableColumn<CarritoItem, String> colSubtotal;

    @FXML
    private TableView<Producto> tablaResultados;

    @FXML
    private TableColumn<Producto, String> colNombreResultado;

    @FXML
    private TableColumn<Producto, String> colPrecioResultado;

    @FXML
    private TableColumn<Producto, String> colStockResultado;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtMontoRecibido;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblVuelto;

    @FXML
    private ComboBox<MetodoPago> cmbMetodoPago;

    private final ObservableList<CarritoItem> carrito = FXCollections.observableArrayList();
    private final ObservableList<Producto> resultados = FXCollections.observableArrayList();

    /**
     * Inicializa las tablas, el combo de pago y los listeners.
     */
    @FXML
    private void initialize() {
        colProducto.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colPrecio.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPrecio().toString()));
        colCantidad.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getCantidad())));
        colSubtotal.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getSubtotal().toString()));
        tablaCarrito.setItems(carrito);

        colNombreResultado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colPrecioResultado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPrecioVenta().toString()));
        colStockResultado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getStock())));
        tablaResultados.setItems(resultados);

        cmbMetodoPago.setItems(FXCollections.observableArrayList(MetodoPago.values()));
        cmbMetodoPago.getSelectionModel().select(MetodoPago.EFECTIVO);

        cmbMetodoPago.valueProperty().addListener((obs, oldVal, newVal) -> {
            txtMontoRecibido.setDisable(newVal != MetodoPago.EFECTIVO);
            actualizarTotales();
        });

        txtMontoRecibido.textProperty().addListener((obs, oldVal, newVal) -> actualizarTotales());

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
                tablaCarrito.refresh();
                actualizarTotales();
                return;
            }
        }
        carrito.add(new CarritoItem(producto, 1));
        actualizarTotales();
    }

    /**
     * Incrementa la cantidad del ítem seleccionado.
     */
    @FXML
    private void onIncrementar() {
        cambiarCantidad(1);
    }

    /**
     * Decrementa la cantidad del ítem seleccionado.
     */
    @FXML
    private void onDecrementar() {
        cambiarCantidad(-1);
    }

    private void cambiarCantidad(int delta) {
        CarritoItem item = tablaCarrito.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
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
        tablaCarrito.refresh();
        actualizarTotales();
    }

    /**
     * Quita el ítem seleccionado del carrito.
     */
    @FXML
    private void onQuitar() {
        CarritoItem item = tablaCarrito.getSelectionModel().getSelectedItem();
        if (item != null) {
            carrito.remove(item);
            actualizarTotales();
        }
    }

    /**
     * Vacía el carrito.
     */
    @FXML
    private void onVaciar() {
        carrito.clear();
        actualizarTotales();
    }

    private void actualizarTotales() {
        BigDecimal total = totalCarrito();
        lblTotal.setText("Total: S/ " + total.toPlainString());
        if (cmbMetodoPago.getValue() == MetodoPago.EFECTIVO) {
            calcularVuelto(total);
        } else {
            lblVuelto.setText("Vuelto: S/ 0.00");
        }
    }

    private void calcularVuelto(BigDecimal total) {
        String monto = txtMontoRecibido.getText().trim();
        if (monto.isEmpty()) {
            lblVuelto.setText("Vuelto: S/ 0.00");
            return;
        }
        try {
            BigDecimal recibido = new BigDecimal(monto);
            lblVuelto.setText("Vuelto: S/ " + recibido.subtract(total).toPlainString());
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

        MetodoPago metodo = cmbMetodoPago.getValue();
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
