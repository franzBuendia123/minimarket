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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del módulo de reportes (ventas del día y stock bajo).
 */
public class ReporteController {

    private final VentaService ventaService;
    private final ProductoService productoService;

    public ReporteController(VentaService ventaService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    @FXML
    private Label lblTotalHoy;

    @FXML
    private TableView<Venta> tablaVentas;

    @FXML
    private TableColumn<Venta, String> colIdVenta;

    @FXML
    private TableColumn<Venta, String> colFechaVenta;

    @FXML
    private TableColumn<Venta, String> colTotalVenta;

    @FXML
    private TableColumn<Venta, String> colMetodoVenta;

    @FXML
    private TableView<Producto> tablaStockBajo;

    @FXML
    private TableColumn<Producto, String> colNombreStock;

    @FXML
    private TableColumn<Producto, String> colStockBajo;

    /**
     * Inicializa las tablas y carga los reportes.
     */
    @FXML
    private void initialize() {
        colIdVenta.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getIdVenta())));
        colFechaVenta.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getFecha() == null ? "" : cell.getValue().getFecha().toString()));
        colTotalVenta.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTotal().toPlainString()));
        colMetodoVenta.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getMetodoPago())));

        colNombreStock.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colStockBajo.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getStock())));

        cargar();
    }

    private void cargar() {
        try {
            LocalDate hoy = LocalDate.now();
            BigDecimal total = ventaService.sumTotalByFecha(hoy, hoy);
            lblTotalHoy.setText("Total de ventas de hoy: S/ " + total.toPlainString());
            tablaVentas.setItems(FXCollections.observableArrayList(ventaService.findByFecha(hoy, hoy)));
            tablaStockBajo.setItems(FXCollections.observableArrayList(productoService.findByStockBajo(5)));
        } catch (Exception e) {
            AlertUtil.mostrarError("Reportes", e.getMessage());
        }
    }

    /**
     * Recarga los reportes.
     */
    @FXML
    private void onActualizar() {
        cargar();
    }

    /**
     * Exporta las ventas del día a un PDF.
     */
    @FXML
    private void onExportarPDF() {
        try {
            List<String> encabezados = List.of("Id", "Fecha", "Total", "Método");
            List<List<String>> filas = new ArrayList<>();
            for (Venta v : tablaVentas.getItems()) {
                filas.add(List.of(String.valueOf(v.getIdVenta()),
                        v.getFecha() == null ? "" : v.getFecha().toString(),
                        v.getTotal().toPlainString(),
                        String.valueOf(v.getMetodoPago())));
            }
            new ReportePDFGenerator().generarReporte("reportes/reporte_ventas_hoy.pdf",
                    "Reporte de ventas del día", encabezados, filas);
            AlertUtil.mostrarInfo("Reportes", "Reporte generado en reportes/reporte_ventas_hoy.pdf");
        } catch (Exception e) {
            AlertUtil.mostrarError("Reportes", e.getMessage());
        }
    }
}
