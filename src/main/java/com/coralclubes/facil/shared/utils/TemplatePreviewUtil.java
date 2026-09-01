package com.coralclubes.facil.shared.utils;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Utilidad autoejecutable para renderizar la plantilla de Tarjeta de Registro Check-In
 * con datos de prueba y generar el preview HTML en target/preview-output.html.
 */
public class TemplatePreviewUtil {

    private static final Map<String, Object> context = Map.ofEntries(
            // Identidad y Encabezado
            Map.entry("logoUrl", "http://10.10.3.207:9000/facil-publico/FACIL/sistema/logos/cuernavaca/abf737fa-5672-4006-b879-26094e642975.jpg"),
            Map.entry("desarrollo", "CORAL CUERNAVACA"),

            // Control de Habitación y Membresía
            Map.entry("habitacion", "MASTER SUITE"),
            Map.entry("socio", "1-12430-1"),
            Map.entry("pax", "6"),
            Map.entry("folio", "10"),

            // Datos del Huésped
            Map.entry("nombre", "RUIZ RAMOS JOSEYN AIDEE"),
            Map.entry("fechaLlegada", "31/08/2026"),
            Map.entry("fechaSalida", "03/09/2026"),
            // Map.entry("direccion", "AV. REVOLUCIÓN 1420, COL. GUADALUPE INN"),
            // Map.entry("cp", "01020"),
            // Map.entry("ciudad", "CIUDAD DE MÉXICO"),
            // Map.entry("pais", "MÉXICO"),
            Map.entry("telefono", "55 4920 1823"),
            Map.entry("email", "jruiz@scanda.com.mx"),
            Map.entry("logoBgColor", "#0f3a6f")
    );

    public static void main(String[] args) {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates/");
        loader.setSuffix(".html");

        PebbleEngine engine = new PebbleEngine.Builder()
                .loader(loader)
                .cacheActive(false)
                .build();

        try {
            // Carga src/main/resources/templates/preview-template.html
            PebbleTemplate compiledTemplate = engine.getTemplate("preview-template");

            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);
            String renderedHtml = writer.toString();

            Path targetDir = Path.of("target");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path outputPath = targetDir.resolve("preview-output.html");
            Files.writeString(outputPath, renderedHtml);

            System.out.println("HTML renderizado correctamente.");
            System.out.println("Archivo de salida: " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error al procesar la plantilla Pebble: " + e.getMessage());
            e.printStackTrace();
        }
    }
}