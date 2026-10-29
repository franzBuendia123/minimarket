package com.lacanasta.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.util.List;

/**
 * Generador de reportes en PDF (iText 7).
 */
public class ReportePDFGenerator {

    /**
     * Constructor por defecto.
     */
    public ReportePDFGenerator() {
    }

    /**
     * Genera un PDF de reporte genérico a partir de encabezados y filas.
     *
     * @param rutaArchivo ruta completa del archivo PDF.
     * @param titulo      título del reporte.
     * @param encabezados encabezados de las columnas.
     * @param filas       datos del reporte.
     */
    public void generarReporte(String rutaArchivo, String titulo, List<String> encabezados, List<List<String>> filas) {
        try {
            crearCarpetaPadre(rutaArchivo);
            PdfWriter writer = new PdfWriter(rutaArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph(titulo).setBold().setFontSize(14));
            doc.add(new Paragraph(" "));

            Table tabla = new Table(encabezados.size());
            for (String h : encabezados) {
                tabla.addCell(cabecera(h));
            }
            for (List<String> fila : filas) {
                for (String celda : fila) {
                    tabla.addCell(new Cell().add(new Paragraph(celda)));
                }
            }
            doc.add(tabla);
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage(), e);
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
