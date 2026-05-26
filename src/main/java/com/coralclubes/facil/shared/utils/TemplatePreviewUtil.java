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
 * */
public class TemplatePreviewUtil {

    private static final Map<String, Object> context = Map.ofEntries(

            Map.entry("desarrollo", "CORAL CLUBES ACAPULCO"),
            Map.entry("fechaEmision", "26/05/2026"),

            Map.entry("titular", "JUAN PÉREZ LÓPEZ"),
            Map.entry("membresia", "0-123456-1"),

            Map.entry("foliosReservacion", "45001, 45002"),

            Map.entry("fechaEntrada", "19/08/2026"),
            Map.entry("fechaSalida", "22/08/2026"),

            Map.entry("observaciones",
                    "Habitación cercana a elevador. " +
                            "Cliente solicita cama king size y check-in anticipado sujeto a disponibilidad."
            ),

            Map.entry("importeTotal", "15,000.00"),

            Map.entry(
                    "habitaciones",
                    List.of(

                            Map.ofEntries(
                                    Map.entry("tipoHabitacion", "MASTER SUITE"),
                                    Map.entry("totalPax", 2)
                            ),

                            Map.ofEntries(
                                    Map.entry("tipoHabitacion", "JR SUITE VISTA AL MAR"),
                                    Map.entry("totalPax", 4)
                            ),

                            Map.ofEntries(
                                    Map.entry("tipoHabitacion", "PENTHOUSE PRESIDENCIAL"),
                                    Map.entry("totalPax", 6)
                            )

                    )
            )

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