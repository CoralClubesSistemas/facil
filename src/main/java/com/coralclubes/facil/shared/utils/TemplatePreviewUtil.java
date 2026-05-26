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
            Map.entry("esCancelado", false),
            Map.entry("esReimpresion", true),
            Map.entry("empresa", "CORAL CLUBES, S.A. DE C.V."),
            Map.entry("rfcEmpresa", "CCL260526XX1"),
            Map.entry("direccionEmpresa", "Av. Reforma 123, Ciudad de México"),
            Map.entry("folio", "REC-2026-8842"),
            Map.entry("clienteNombre", "Luis Ángel Vivar Tovar"),
            Map.entry("membresia", "VIP-99823"),
            Map.entry("fecha", "26/05/2026"),
            Map.entry("tipoDocumento", "Tarjeta de Crédito"),
            Map.entry("moneda", "MXN"),

            Map.entry("movimientos", List.of(
                    Map.of(
                            "descripcion", "Mantenimiento Mensual",
                            "referencia", "REF-001-MAY",
                            "importe", "$ 1,500.00",
                            "interes", "$ 0.00",
                            "descuento", "-$ 150.00",
                            "tieneDescuento", true,
                            "totalNeto", "$ 1,350.00",
                            "claseFila", "row-even"
                    ),
                    Map.of(
                            "descripcion", "Cargo por Servicios Adicionales",
                            "referencia", "REF-002-MAY",
                            "importe", "$ 500.00",
                            "interes", "$ 25.00",
                            "descuento", "$ 0.00",
                            "tieneDescuento", false,
                            "totalNeto", "$ 525.00",
                            "claseFila", "row-odd"
                    )
            )),
            Map.entry("subtotal", "$ 2,000.00"),
            Map.entry("descuentoTotal", "-$ 150.00"),
            Map.entry("total", "$ 1,875.00"),
            Map.entry("cadenaSeguridad", "||3.3|REC|2026-05-26T10:30:00|01|00001000000500000000||")
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