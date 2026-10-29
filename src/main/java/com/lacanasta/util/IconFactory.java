package com.lacanasta.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.SVGPath;

/**
 * Generador de iconos SVG (estilo línea) para la interfaz, basado en el
 * sistema de iconos del diseño Figma (viewBox 24x24, trazo redondeado).
 */
public final class IconFactory {

    private IconFactory() {
    }

    /**
     * Crea un icono con el color indicado, escalado al tamaño deseado.
     *
     * @param name  nombre del icono.
     * @param size  tamaño en píxeles.
     * @param color color del trazo.
     * @return el nodo con el icono.
     */
    public static Node create(String name, double size, Color color) {
        Group group = new Group();
        build(group, name, color);
        double scale = size / 24.0;
        group.setScaleX(scale);
        group.setScaleY(scale);
        return group;
    }

    private static void build(Group group, String name, Color color) {
        switch (name) {
            case "basket" -> {
                group.getChildren().add(path("M5 10h14l-1.2 8.2a2 2 0 0 1-2 1.8H8.2a2 2 0 0 1-2-1.8L5 10Z", color));
                group.getChildren().add(path("m8 10 4-6 4 6M9 14v2m3-2v2m3-2v2", color));
            }
            case "sales" -> {
                group.getChildren().add(path("M4 19V5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v14l-3-2-2 2-3-2-3 2-2-2-3 2Z", color));
                group.getChildren().add(path("M8 7h8m-8 4h6", color));
            }
            case "cash" -> {
                group.getChildren().add(rect(3, 6, 18, 12, 2, color));
                group.getChildren().add(path("M7 10h4m6 4h.01M3 9h18", color));
            }
            case "reports" -> group.getChildren().add(path("M4 20V10m6 10V4m6 16v-7m4 7H2", color));
            case "products" -> {
                group.getChildren().add(path("m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z", color));
                group.getChildren().add(path("m4.5 7.5 7.5 4 7.5-4M12 21v-9.5", color));
            }
            case "purchases" -> {
                group.getChildren().add(path("M3 4h2l2.4 10.2a2 2 0 0 0 2 1.5h7.8a2 2 0 0 0 2-1.6L21 7H6", color));
                group.getChildren().add(path("M10 20h.01M18 20h.01", color));
            }
            case "suppliers" -> group.getChildren().add(path("M3 20h18M5 20V8l7-4 7 4v12M9 12h6m-6 4h6", color));
            case "users" -> {
                group.getChildren().add(path("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2", color));
                group.getChildren().add(path("M9 7a4 4 0 1 0 0 0m13 14v-2a4 4 0 0 0-3-3.9M16 3.1a4 4 0 0 1 0 7.8", color));
            }
            case "settings" -> {
                group.getChildren().add(path("M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z", color));
                group.getChildren().add(path("M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H2.8v-4H3a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1-1.6v-.2h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1Z", color));
            }
            case "search" -> {
                group.getChildren().add(path("M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z", color));
                group.getChildren().add(path("m20 20-4-4", color));
            }
            case "logout" -> group.getChildren().add(path("M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4m7 14 5-5-5-5m5 5H9", color));
            case "plus" -> group.getChildren().add(path("M12 5v14M5 12h14", color));
            case "minus" -> group.getChildren().add(path("M5 12h14", color));
            case "trash" -> group.getChildren().add(path("M3 6h18M8 6V4h8v2m3 0-1 15H6L5 6m5 4v7m4-7v7", color));
            case "package" -> {
                group.getChildren().add(path("m12 2 9 5-9 5-9-5 9-5Z", color));
                group.getChildren().add(path("m3 7 9 5 9-5v10l-9 5-9-5V7Z", color));
            }
            case "trend" -> {
                group.getChildren().add(path("m3 17 6-6 4 4 8-9", color));
                group.getChildren().add(path("M15 6h6v6", color));
            }
            case "receipt" -> {
                group.getChildren().add(path("M5 3h14v18l-3-2-2 2-2-2-2 2-2-2-3 2V3Z", color));
                group.getChildren().add(path("M9 8h6m-6 4h6", color));
            }
            case "warning" -> {
                group.getChildren().add(path("M12 3 2.7 20h18.6L12 3Z", color));
                group.getChildren().add(path("M12 9v4m0 3h.01", color));
            }
            case "arrow" -> group.getChildren().add(path("m9 18 6-6-6-6", color));
            case "check" -> group.getChildren().add(path("m5 12 4 4L19 6", color));
            case "calendar" -> {
                group.getChildren().add(rect(3, 5, 18, 16, 2, color));
                group.getChildren().add(path("M16 3v4M8 3v4M3 10h18", color));
            }
            default -> throw new IllegalArgumentException("Icono desconocido: " + name);
        }
    }

    private static SVGPath path(String d, Color color) {
        SVGPath p = new SVGPath();
        p.setContent(d);
        style(p, color);
        return p;
    }

    private static Rectangle rect(double x, double y, double w, double h, double rx, Color color) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(rx * 2);
        r.setArcHeight(rx * 2);
        style(r, color);
        return r;
    }

    private static void style(Shape shape, Color color) {
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(color);
        shape.setStrokeWidth(1.8);
        shape.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        shape.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
    }
}
