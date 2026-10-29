package com.lacanasta.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.lacanasta.model.DetalleVenta;
import com.lacanasta.model.EmpresaConfig;
import com.lacanasta.model.Venta;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generador de tickets de venta en PDF (iText 7).
 */
public class TicketPDFGenerator {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructor por defecto.
     */
    public TicketPDFGenerator() {
    }

    /**
     * Genera el PDF de un ticket de venta.
     *
     * @param rutaArchivo ruta completa del archivo PDF a generar.
     * @param config      configuración de la empresa (cabecera).
     * @param venta       venta a imprimir.
     * @param detalles    líneas de detalle de la venta.
     * @param vuelto      vuelto a devolver (puede ser cero).
     */
    public void generarTicket(String rutaArchivo, EmpresaConfig config, Venta venta,
                              List<DetalleVenta> detalles, BigDecimal vuelto) {
        try {
            crearCarpetaPadre(rutaArchivo);
            PdfWriter writer = new PdfWriter(rutaArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph(config != null && config.getNombreComercial() != null
                    ? config.getNombreComercial() : "La Canasta").setBold().setFontSize(14));
            if (config != null) {
                if (config.getRuc() != null) {
                    doc.add(new Paragraph("RUC: " + config.getRuc()).setFontSize(9));
                }
                if (config.getDireccion() != null) {
                    doc.add(new Paragraph(config.getDireccion()).setFontSize(9));
                }
            }

            doc.add(new Paragraph("Ticket de venta #" + venta.getIdVenta()).setFontSize(11).setBold());
            doc.add(new Paragraph("Fecha: " + (venta.getFecha() != null
                    ? venta.getFecha().format(FORMATO_FECHA) : "")).setFontSize(9));
            doc.add(new Paragraph(" "));

            Table tabla = new Table(4);
            tabla.addCell(cabecera("Producto"));
            tabla.addCell(cabecera("Cant."));
            tabla.addCell(cabecera("P.U."));
            tabla.addCell(cabecera("Subtotal"));
            for (DetalleVenta d : detalles) {
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(d.getIdProducto()))));
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(d.getCantidad()))));
                tabla.addCell(new Cell().add(new Paragraph(d.getPrecioUnitario().toPlainString())));
                tabla.addCell(new Cell().add(new Paragraph(d.getSubtotal().toPlainString())));
            }
            doc.add(tabla);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("TOTAL: S/ " + venta.getTotal().toPlainString()).setBold().setFontSize(12));
            doc.add(new Paragraph("Método de pago: " + venta.getMetodoPago()).setFontSize(9));
            if (vuelto != null && vuelto.compareTo(BigDecimal.ZERO) > 0) {
                doc.add(new Paragraph("Vuelto: S/ " + vuelto.toPlainString()).setFontSize(9));
            }
            if (config != null && config.getMensajeTicket() != null) {
                doc.add(new Paragraph(config.getMensajeTicket()).setFontSize(9).setItalic());
            }

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    private Cell cabecera(String texto) {
        return new Cell().add(new Paragraph(texto).setBold().setFontSize(9));
    }

    private void crearCarpetaPadre(String rutaArchivo) {
        File padre = new File(rutaArchivo).getParentFile();
        if (padre != null && !padre.exists()) {
            padre.mkdirs();
        }
    }
}
