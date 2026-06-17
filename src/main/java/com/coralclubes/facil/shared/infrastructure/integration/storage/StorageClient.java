package com.coralclubes.facil.shared.infrastructure.integration.storage;

import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.*;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;

/**
 * Cliente de integración para comunicarse con el microservicio de almacenamiento (Coral Almacenamiento).
 * Proporciona soporte tanto para el flujo moderno asíncrono (Valet Key) como para el flujo legacy síncrono.
 */
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
     * Negocia una URL de carga directa con el microservicio de almacenamiento (flujo asíncrono - Valet Key).
     *
     * @param solicitud Metadatos y detalles del archivo a subir (nombre, tamaño, alias de configuración, etc.).
     * @return La respuesta de carga conteniendo la URL prefirmada de subida directa (uploadUrl) y el identificador único.
     * @throws ServiceUnavailableException Si el servicio de almacenamiento no responde o responde con un error.
     */
    public RespuestaCargaDto solicitarUrlCarga(SolicitudCargaDto solicitud) {
        try {

            ApiResponse<RespuestaCargaDto> response = restClient.post()
                    .uri(serviceUrl + "/api/v1/storage/sign-upload")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

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
     * Solicita firmas de carga por lote (Batch) en el flujo asíncrono para múltiples archivos.
     *
     * @param batchDto Lista de solicitudes de carga individuales.
     * @return Objeto que contiene las respuestas de carga exitosas y los fallidos con su detalle.
     * @throws ServiceUnavailableException Si el servicio de almacenamiento no responde o responde con un error.
     */
    public RespuestaBatchDto<RespuestaCargaDto> solicitarCargaBatch(SolicitudCargaBatchDto batchDto) {
        try {
            ApiResponse<RespuestaBatchDto<RespuestaCargaDto>> response = restClient.post()
                    .uri(serviceUrl + "/api/v1/storage/sign-upload/batch")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(batchDto)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.data() != null) {
                return response.data();
            }

            throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al solicitar carga batch: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Obtiene la URL temporal de descarga para un archivo específico (pública directa o firmada si es privado).
     *
     * @param uuid El identificador único del archivo.
     * @return La URL final para la descarga del archivo.
     * @throws ServiceUnavailableException Si el servicio de almacenamiento no responde o responde con un error.
     */
    public ArchivoDescarga obtenerUrlDescarga(UUID uuid) {
        return obtenerUrlDescarga(uuid, "inline");
    }

    public ArchivoDescarga obtenerUrlDescarga(UUID uuid, String disposition) {
        try {
            String uri = serviceUrl + "/api/v1/storage/files/" + uuid;
            if (disposition != null) {
                uri += "?disposition=" + disposition;
            }

            ApiResponse<InfoArchivoDto> response = restClient.get()
                    .uri(uri)
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.data() != null) {
                return new ArchivoDescarga(response.data().nombreOriginal(), response.data().urlDescarga());
            }

            throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al negociar URL de descarga: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Consulta detalles y URLs de descarga por lote (Batch) de varios archivos a la vez.
     *
     * @param batchDto Lista de UUIDs de archivos a consultar.
     * @return Objeto con los detalles de descarga exitosos y fallidos.
     * @throws ServiceUnavailableException Si el servicio de almacenamiento no responde o responde con un error.
     */
    public RespuestaBatchDto<InfoArchivoDto> consultarArchivosBatch(SolicitudDescargaBatchDto batchDto) {
        try {
            ApiResponse<RespuestaBatchDto<InfoArchivoDto>> response = restClient.post()
                    .uri(serviceUrl + "/api/v1/storage/files/batch")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(batchDto)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.data() != null) {
                return response.data();
            }

            throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al consultar archivos batch: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Obtiene la URL de descarga de un archivo y su nombre original, facilitando la descarga en el frontend.
     *
     * @param uuid El identificador único del archivo.
     * @return Un objeto {@link ArchivoDescarga} con el nombre original y la URL temporal de descarga.
     * @throws ServiceUnavailableException Si el servicio de almacenamiento no responde o responde con un error.
     */
    public ArchivoDescarga obtenerUrlDescargaYNombre(UUID uuid) {
        return obtenerUrlDescargaYNombre(uuid, "inline");
    }

    public ArchivoDescarga obtenerUrlDescargaYNombre(UUID uuid, String disposition) {
        try {
            String uri = serviceUrl + "/api/v1/storage/files/" + uuid;
            if (disposition != null) {
                uri += "?disposition=" + disposition;
            }

            ApiResponse<InfoArchivoDto> response = restClient.get()
                    .uri(uri)
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.data() != null) {
                ArchivoDescarga resultado = new ArchivoDescarga(response.data().nombreOriginal(), response.data().urlDescarga());
                return resultado;
            }

            throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al negociar URL de descarga: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }

    /**
     * Solicita la eliminación lógica o física de un archivo en el microservicio.
     *
     * @param uuid      El identificador único del archivo a eliminar.
     * @param permanent {@code true} si la eliminación debe ser física definitiva en el bucket, {@code false} para lógica.
     * @throws ServiceUnavailableException Si el servicio de almacenamiento no responde o responde con un error.
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
     * Realiza la subida física del archivo binario directamente al almacenamiento (MinIO / S3)
     * utilizando una URL prefirmada negociada previamente (Paso 2 de Valet Key).
     *
     * @param uploadUrl   URL prefirmada obtenida en la negociación de carga.
     * @param archivo     El arreglo de bytes del archivo a subir.
     * @param contentType El tipo MIME del contenido del archivo.
     * @throws ServiceUnavailableException Si falla la subida directa del binario.
     */
    public void subirArchivoBinario(String uploadUrl, byte[] archivo, String contentType) {

        try {

            HttpURLConnection connection = (HttpURLConnection) URI.create(uploadUrl).toURL().openConnection();

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

    /**
     * Carga un archivo de forma síncrona en el microservicio de almacenamiento (flujo legacy).
     * Envía tanto el binario del archivo como sus metadatos asociados en una sola petición multipart.
     * Útil cuando el backend mismo genera el archivo y no nos preocupa el tiempo de transferencia.
     *
     * @param archivoBytes    Contenido binario del archivo en un arreglo de bytes.
     * @param nombreArchivo   Nombre original del archivo (ej. "reporte.xlsx").
     * @param contentType     Tipo de contenido del archivo (ej. "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").
     * @param solicitudLegacy Datos y configuraciones del archivo (canal/alias, metadatos personalizados, etc.).
     * @return Información detallada del archivo una vez persistido y disponible en el storage.
     * @throws ServiceUnavailableException Si el servicio de almacenamiento no responde o responde con un error.
     */
    public InfoArchivoDto cargarArchivoSincrono(byte[] archivoBytes, String nombreArchivo, String contentType, SolicitudCargaLegacyDto solicitudLegacy) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(archivoBytes) {
                @Override
                public String getFilename() {
                    return nombreArchivo;
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            if (contentType != null) {
                fileHeaders.setContentType(MediaType.parseMediaType(contentType));
            }
            HttpEntity<Resource> filePart = new HttpEntity<>(fileResource, fileHeaders);

            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SolicitudCargaLegacyDto> metadataPart = new HttpEntity<>(solicitudLegacy, metadataHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", filePart);
            body.add("metadata", metadataPart);

            ApiResponse<InfoArchivoDto> response = restClient.post()
                    .uri(serviceUrl + "/api/v1/storage/legacy/upload-sync")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.data() != null) {
                return response.data();
            }

            throw new IllegalStateException("El microservicio de storage devolvió una respuesta vacía.");

        } catch (Exception e) {
            logger.error("STORAGE_CLIENT", "Error al cargar archivo síncrono (legacy): " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de almacenamiento no está disponible en este momento.");
        }
    }
}