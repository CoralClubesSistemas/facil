package com.coralclubes.facil.shared.utils;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Clase de utilidad autoejecutable que permite renderizar una plantilla Pebble con datos de ejemplo para generar un archivo HTML de preview.
 * Para usarla correctamente:
 * <p>
 * 1. Coloca tu plantilla Pebble de preview en src/main/resources/templates/ con el nombre "preview-template.html".
 * 2. Inserta datos de ejemplo en el mapa de contexto en la variable estatica "context" para simular la información que normalmente se pasaría desde el backend.
 * 3. Ejecuta esta clase como una aplicación Java. Se generará un archivo HTML en target/preview-output.html con el resultado renderizado.
 * 4. Abre el archivo generado en un navegador para visualizar el resultado.
 *
 */
public class TemplatePreviewUtil {

    private static final Map<String, Object> context = Map.ofEntries(
            // Encabezado principal
            Map.entry("razonSocial", "CORAL CLUBES"),
            Map.entry("slogan", "CREANDO MOMENTOS INOLVIDABLES"),
            Map.entry("periodoInicio", "01/06/2026"),
            Map.entry("periodoFin", "30/06/2026"),
            Map.entry("fechaEmision", "02/07/2026"),

            // Datos generales del Socio
            Map.entry("titular", "Alejandro Gómez Ruiz"),
            Map.entry("membresia", "CC-98234"),
            Map.entry("tipoMembresia", "Familiar Premium"),
            Map.entry("telefonoContacto", "+52 55 1234 5678"),
            Map.entry("correoContacto", "alejandro.gomez@email.com"),
            Map.entry("domicilioSocio", "Av. Paseo de la Reforma 412, Lomas de Chapultepec, CDMX, C.P. 11000"),

            // Listado de movimientos de la tabla
            Map.entry(
                    "movimientos",
                    List.of(
                            Map.ofEntries(
                                    Map.entry("fecha", "2026-06-01"),
                                    Map.entry("concepto", "Mantenimiento Mencual Familiar - Junio"),
                                    Map.entry("montoCargo", "3,500.00"),
                                    Map.entry("montoInteres", "0.00"),
                                    Map.entry("montoAbonado", "0.00"),
                                    Map.entry("montoPendiente", "0.00")
                            ),
                            Map.ofEntries(
                                    Map.entry("fecha", "2026-06-05"),
                                    Map.entry("concepto", "Clase Particular de Tenis (Instructor Senior)"),
                                    Map.entry("montoCargo", "850.00"),
                                    Map.entry("montoInteres", "42.50"),
                                    Map.entry("montoAbonado", "0.00"),
                                    Map.entry("montoPendiente", "892.50")
                            )
                    )
            ),

            // Desglose final de saldos
            Map.entry(
                    "resumenTotales",
                    Map.ofEntries(
                            Map.entry("totalCargos", "7,540.00"),
                            Map.entry("totalInteres", "67.50"),
                            Map.entry("totalAbonado", "4,500.00"),
                            Map.entry("totalNetoExigible", "3,107.50")
                    )
            )
    );

    public static void main(String[] args) {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates/");
        loader.setSuffix(".html");

        PebbleEngine engine = new PebbleEngine.Builder().loader(loader).cacheActive(false).build();

        try {
            PebbleTemplate compiledTemplate = engine.getTemplate("preview-template");

            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);
            String renderedHtml = writer.toString();

            System.out.println("====================================================");
            System.out.println(" HTML RENDERIZADO CON ÉXITO (MODO TEXTO PLANO) ");
            System.out.println("====================================================");

            Path outputPath = Path.of("target/preview-output.html");
            Files.writeString(outputPath, renderedHtml);

            System.out.println("Archivo de preview generado en: " + outputPath.toAbsolutePath());
            System.out.println("====================================================");

        } catch (IOException e) {
            System.err.println("Error crítico al procesar o guardar la plantilla Pebble: " + e.getMessage());
            e.printStackTrace();
        }
    }
}