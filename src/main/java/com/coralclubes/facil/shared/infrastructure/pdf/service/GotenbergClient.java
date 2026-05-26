package com.coralclubes.facil.shared.infrastructure.pdf.service;

import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * Conexion con el contenedor de Gotemberg para la generación de PDFs a partir de HTML y CSS.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GotenbergClient {
    @Value("${app.clients.gotenberg.url}")
    private String gotenbergUrl;

    /**
     * Convierte contenido HTML (y opcionalmente CSS) a PDF utilizando Gotenberg.
     *
     * @param htmlContent Contenido HTML a convertir (requerido).
     * @param cssContent  Contenido CSS para estilos (opcional), aun que los estilos pueden venir embebidos en el HTML.
     * @return Byte array del PDF generado.
     */
    public byte[] convertHtmlToPdf(String htmlContent, String cssContent) {
        // creamos un RestClient específico para esta operación, apuntando a la URL de Gotenberg
        RestClient restClient = RestClient.builder()
                .baseUrl(gotenbergUrl)
                .build();

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("files", new NamedByteArrayResource(htmlContent.getBytes(StandardCharsets.UTF_8), "index.html"));

            if (cssContent != null && !cssContent.isBlank()) {
                body.add("files", new NamedByteArrayResource(cssContent.getBytes(StandardCharsets.UTF_8), "style.css"));
            }

            byte[] responseBytes = restClient.post()
                    .uri("/forms/chromium/convert/html") // endpoint específico para conversión de HTML a PDF
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(byte[].class);

            if (responseBytes == null || responseBytes.length == 0) {
                throw new RuntimeException("Gotenberg devolvió un PDF vacío");
            }

            return responseBytes;
        } catch (Exception e) {
            log.error("[GOTENBERG] Error al llamar a Gotenberg: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de generación de PDFs (Gotenberg) no está disponible en este momento.");
        }
    }

    /** Sobrecarga para casos donde solo se tiene HTML sin CSS adicional */
    public byte[] convertHtmlToPdf(String htmlContent) {
        return convertHtmlToPdf(htmlContent, null);
    }

    /**
     * Clase interna para representar un recurso de byte array con un nombre de archivo, necesario para enviar archivos a Gotenberg en formato multipart/form-data.
     */
    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        public NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }
}
