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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageClient {

    private final BusinessLogger logger;

    private final RestClient restClient;

    @Value("${app.clients.storage.url}")
    private String serviceUrl;

    @Value("${app.clients.storage.api-key}")
    private String apiKey;

    /**
     * Negocia una URL de carga directa con el microservicio de almacenamiento.
     */
    public RespuestaCargaDto solicitarUrlCarga(SolicitudCargaDto solicitud) {
        try {

            ApiResponse<RespuestaCargaDto> response = restClient.post()
                    .uri(serviceUrl + "/api/v1/storage/sign-upload")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.data() != null) {
                return response.data();
            }

            throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al negociar URL de carga: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Obtiene la URL de descarga de un archivo.
     */
    public String obtenerUrlDescarga(UUID uuid) {
        try {

            ApiResponse<InfoArchivoDto> response = restClient.get()
                    .uri(serviceUrl + "/api/v1/storage/files/" + uuid)
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.data() != null) {
                return response.data().urlDescarga();
            }

            throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al negociar URL de descarga: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Elimina un archivo del almacenamiento.
     */
    public void eliminarArchivo(UUID uuid, boolean permanent) {
        try {

            restClient.delete()
                    .uri(serviceUrl + "/api/v1/storage/files/" + uuid + "?permanent=" + permanent)
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("STORAGE_CLIENT", "Archivo eliminado exitosamente: " + uuid);

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al eliminar archivo: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Realiza la carga directa de un archivo binario a una URL prefirmada (MinIO / S3).
     * Paso 2 del patrón Valet Key.
     */
    public void subirArchivoBinario(String uploadUrl, byte[] archivo, String contentType) {

        try {

            HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();

            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);

            // Headers EXACTOS que firmó MinIO
            connection.setRequestProperty("Content-Type", contentType);

            // obligatorio para S3/MinIO
            connection.setFixedLengthStreamingMode(archivo.length);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(archivo);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode >= 400) {
                throw new RuntimeException("Error en upload a storage. HTTP " + responseCode);
            }

            logger.info("STORAGE_CLIENT", "Archivo binario subido exitosamente a la URL prefirmada.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al subir el archivo binario directo al bucket: " + e.getMessage(), e);
            throw new ServiceUnavailableException("Fallo la carga directa al bucket de almacenamiento.");
        }
    }
}