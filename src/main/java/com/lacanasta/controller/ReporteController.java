package com.lacanasta.controller;

import com.lacanasta.model.Producto;
import com.lacanasta.model.Venta;
import com.lacanasta.service.ProductoService;
import com.lacanasta.service.VentaService;
import com.lacanasta.util.AlertUtil;
import com.lacanasta.util.ReportePDFGenerator;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del módulo de reportes (métricas, ventas del día y stock bajo).
 */
public class ReporteController {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final VentaService ventaService;
    private final ProductoService productoService;

    public ReporteController(VentaService ventaService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblVentasDia;

    @FXML
    private Label lblTransacciones;

    @FXML
    private Label lblProductosVendidos;

    @FXML
    private Label lblStockBajo;

    @FXML
    private TableView<Venta> tablaVentas;

    @FXML
    private TableColumn<Venta, String> colTicket;

    @FXML
    private TableColumn<Venta, String> colHora;

    @FXML
    private TableColumn<Venta, String> colMedio;

    @FXML
    private TableColumn<Venta, String> colTotal;

    @FXML
    private TableColumn<Venta, String> colEstado;

    @FXML
    private TableView<Producto> tablaStockBajo;

    @FXML
    private TableColumn<Producto, String> colNombreStock;

    @FXML
    private TableColumn<Producto, String> colStockBajo;

    @FXML
    private void initialize() {
        colTicket.setCellValueFactory(cell -> new ReadOnlyStringWrapper("V-" + cell.getValue().getIdVenta()));
        colHora.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getFecha() == null ? "" : cell.getValue().getFecha().format(FMT_HORA)));
        colMedio.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getMetodoPago())));
        colTotal.setCellValueFactory(cell -> new ReadOnlyStringWrapper("S/ " + cell.getValue().getTotal().toPlainString()));
        colEstado.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getEstadoPago())));
        colEstado.setCellFactory(col -> new TableCell<Venta, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String texto = item.substring(0, 1) + item.substring(1).toLowerCase();
                    Label badge = new Label(texto);
                    badge.getStyleClass().addAll("badge", "badge-success");
                    setGraphic(badge);
                }
            }
        });

        colNombreStock.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colStockBajo.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getStock())));

        cargar();
    }

    private void cargar() {
        try {
            LocalDate hoy = LocalDate.now();
            lblFecha.setText("Hoy, " + hoy.format(FMT_FECHA));

            BigDecimal total = ventaService.sumTotalByFecha(hoy, hoy);
            List<Venta> ventas = ventaService.findByFecha(hoy, hoy);
            int unidadesVendidas = ventaService.sumCantidadVendidaByFecha(hoy, hoy);
            List<Producto> stockBajo = productoService.findByStockBajo(5);

            lblVentasDia.setText("S/ " + total.toPlainString());
            lblTransacciones.setText(String.valueOf(ventas.size()));
            lblProductosVendidos.setText(String.valueOf(unidadesVendidas));
            lblStockBajo.setText(String.valueOf(stockBajo.size()));

            tablaVentas.setItems(FXCollections.observableArrayList(ventas));
            tablaStockBajo.setItems(FXCollections.observableArrayList(stockBajo));
        } catch (Exception e) {
            AlertUtil.mostrarError("Reportes", e.getMessage());
        }
    }

    @FXML
    private void onActualizar() {
        cargar();
    }

    @FXML
    private void onRevisarInventario() {
        MainController.irAProductos();
    }

    @FXML
    private void onExportarPDF() {
        try {
            List<String> encabezados = List.of("Ticket", "Hora", "Medio de pago", "Total", "Estado");
            List<List<String>> filas = new ArrayList<>();
            for (Venta v : tablaVentas.getItems()) {
                filas.add(List.of("V-" + v.getIdVenta(),
                        v.getFecha() == null ? "" : v.getFecha().format(FMT_HORA),
                        String.valueOf(v.getMetodoPago()),
                        "S/ " + v.getTotal().toPlainString(),
                        String.valueOf(v.getEstadoPago())));
            }
            new ReportePDFGenerator().generarReporte("reportes/reporte_ventas_hoy.pdf",
                    "Reporte de ventas del día", encabezados, filas);
            AlertUtil.mostrarInfo("Reportes", "Reporte generado en reportes/reporte_ventas_hoy.pdf");
        } catch (Exception e) {
            AlertUtil.mostrarError("Reportes", e.getMessage());
        }
    }
}
