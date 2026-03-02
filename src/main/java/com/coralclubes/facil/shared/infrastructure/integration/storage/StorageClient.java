package com.coralclubes.facil.shared.infrastructure.integration.storage;

import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageClient {

    private final BusinessLogger logger;

    @Value("${app.clients.storage.url}")
    private String serviceUrl;

    @Value("${app.clients.storage.api-key}")
    private String apiKey;

    /**
     * Negocia una URL de carga directa con el microservicio de almacenamiento.
     */
    public RespuestaCargaDto solicitarUrlCarga(SolicitudCargaDto solicitud) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();

        try {
            ApiResponse<RespuestaCargaDto> response = restClient.post()
                    .uri("/api/v1/storage/sign-upload")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.data() != null) {
                return response.data();
            } else {
                throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");
            }

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al negociar URL de carga: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    public String obtenerUrlDescarga(UUID uuid) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();

        try {
            ApiResponse<InfoArchivoDto> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/storage/files/" + uuid.toString())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.data() != null) {
                return response.data().urlDescarga();
            } else {
                throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");
            }

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al negociar URL de descarga: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Elimina un archivo del almacenamiento.
     *
     * @param uuid      Identificador del archivo.
     * @param permanent true para borrado físico, false para borrado lógico.
     */
    public void eliminarArchivo(UUID uuid, boolean permanent) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();

        try {
            restClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/storage/files/" + uuid.toString())
                            .queryParam("permanent", permanent)
                            .build())
                    .retrieve()
                    .toBodilessEntity();

            logger.info("STORAGE_CLIENT", "Archivo eliminado exitosamente: " + uuid);

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al eliminar archivo: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }
}