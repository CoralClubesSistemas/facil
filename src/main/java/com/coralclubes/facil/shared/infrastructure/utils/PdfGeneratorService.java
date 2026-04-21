package com.coralclubes.facil.shared.infrastructure.utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final SpringTemplateEngine templateEngine;

    /**
     * Convierte una plantilla Thymeleaf a un arreglo de bytes (PDF).
     *
     * @param templateName Nombre de la plantilla en resources/templates (ej. "CARTA_OCUPACION")
     * @param variables    Mapa con los datos que espera la plantilla
     * @return Arreglo de bytes del PDF generado
     */
    public byte[] generarPdfDesdeHtml(String templateName, Map<String, Object> variables) {

        // 1. Inyectar variables al contexto de Thymeleaf
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }

        // 2. Procesar la plantilla HTML (resources/templates/...)
        String htmlProcesado;
        try {
            htmlProcesado = templateEngine.process(templateName, context);
        } catch (Exception e) {
            throw new RuntimeException("Error al renderizar la plantilla HTML: " + templateName, e);
        }

        // 3. Convertir el HTML a PDF usando OpenHTMLToPDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Cambiamos la forma de obtener el baseUri
            builder.withHtmlContent(htmlProcesado, "/");

            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al convertir el HTML a PDF: " + e.getMessage(), e);
        }
    }
}