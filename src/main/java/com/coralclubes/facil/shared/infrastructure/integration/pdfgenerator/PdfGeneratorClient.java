package com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator;

import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator.dto.GeneratePdfRequest;
import com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator.dto.PdfDataResponse;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Conexion con el microservicio de generacion de pdfs
 * */
@Component
@RequiredArgsConstructor
public class PdfGeneratorClient {

    private final BusinessLogger logger;

    @Value("${app.clients.pdf-generator.url}")
    private String serviceUrl;

    @Value("${app.clients.pdf-generator.api-key}")
    private String apiKey;

    public String generarYSubir(GeneratePdfRequest request) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();

        try {
            // Hacemos el POST
            ApiResponse<PdfDataResponse> response = restClient.post()
                    .uri("/pdfs/generate-upload")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<PdfDataResponse>>() {});

            if (response != null && response.data() != null) {
                logger.info("PDF_CLIENT", "PDF generado exitosamente. URL/ID: " + response.data());
                return response.data().fileId();
            }

        } catch (Exception e) {
            logger.error("PDF_CLIENT", "Error al generar y subir PDF: " + e.getMessage());
            throw new ServiceUnavailableException("El servicio de generación de PDFs no está disponible en este momento. Por favor, inténtalo de nuevo más tarde.");
        }

        return null;
    }

    public byte[] generarYObtener(GeneratePdfRequest request) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();

        try {
            // Hacemos el POST
            byte[] response = restClient.post()
                    .uri("/pdfs/generate-stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(byte[].class);

            if (response != null && response.length > 0) {
                logger.info("PDF_CLIENT", "PDF generado y obtenido exitosamente. Tamaño: " + response.length + " bytes");
                return response;
            }

        } catch (Exception e) {
            logger.error("PDF_CLIENT", "Error al generar y subir PDF: " + e.getMessage());
            throw new ServiceUnavailableException("El servicio de generación de PDFs no está disponible en este momento. Por favor, inténtalo de nuevo más tarde.");
        }

        return null;
    }
}
