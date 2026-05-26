package com.coralclubes.facil.shared.infrastructure.pdf.service;

import com.coralclubes.facil.shared.infrastructure.pdf.dto.PlantillaPdfProjection;
import com.coralclubes.facil.shared.infrastructure.pdf.repository.PlantillasPdfRepository;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Clase principal encargada de la generacion de pdf a partir de platillas guardadas en la
 * base de datos, utilizando pebble como motor de renderizado de html y gotenberg para la conversion a pdf.
 * */
@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final PebbleEngine pebbleEngine;
    private final GotenbergClient gotenbergClient;
    private final PlantillasPdfRepository plantillasRepository;

    /**
     * Convierte una plantilla almacenada en base de datos a un arreglo de bytes (PDF) usando Pebble y Gotenberg.
     *
     * @param templateName Nombre/Código de la plantilla en la base de datos (ej. "CARTA_OCUPACION")
     * @param variables    Mapa con los datos que espera la plantilla
     * @return Arreglo de bytes del PDF generado
     */
    public byte[] generarPdfDesdeHtml(String templateName, Map<String, Object> variables) {
        // 1. Obtener la plantilla de la base de datos
        PlantillaPdfProjection plantilla = plantillasRepository.obtenerPorCodigo(templateName)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la plantilla activa con código: " + templateName));

        // 2. Procesar el HTML con Pebble usando StringLoader (el contenido es la clave de caché y la plantilla en sí)
        String htmlProcesado;
        try {
            Writer writer = new StringWriter();
            PebbleTemplate template = pebbleEngine.getTemplate(plantilla.contenido());
            template.evaluate(writer, variables);
            htmlProcesado = writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al renderizar la plantilla HTML con Pebble: " + templateName, e);
        }

        // 3. Convertir el HTML y CSS a PDF usando Gotenberg
        return gotenbergClient.convertHtmlToPdf(htmlProcesado);
    }
}
