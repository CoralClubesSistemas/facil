package com.coralclubes.facil.shared.infrastructure.integration.notifications;

import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.RespuestaNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Conexion con el microservicio de notificaciones (Coral Notificaciones).
 * Este cliente se encarga de enviar solicitudes de notificación al servicio externo.
 * Utiliza RestClient para realizar las llamadas HTTP y maneja la configuración de la URL y la API Key a través de propiedades.
 * El metodo enviarNotificacion implementa un patrón Fire-and-Forget, lo que significa que no bloquea el flujo de negocio si la respuesta es 202 (Aceptada).
 * */
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final BusinessLogger logger;

    @Value("${app.clients.notifications.url}")
    private String serviceUrl;

    @Value("${app.clients.notifications.api-key}")
    private String apiKey;

    /**
     * Envía una solicitud al microservicio de notificaciones.
     * Patrón Fire-and-Forget (no bloqueamos si la respuesta es 202).
     */
    public void enviarNotificacion(SolicitudNotificacionDto solicitud) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();

        try {
            // Hacemos el POST
            ApiResponse<RespuestaNotificacionDto> response = restClient.post()
                    .uri("/api/v1/notificaciones/enviar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    // Mapeamos la respuesta genérica ApiResponse<RespuestaNotificacionDto>
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.data() != null) {
                logger.info("NOTIF_CLIENT", "Notificación encolada. TrackingId: {}", response.data().trackingId());
            }

        } catch (Exception e) {
            logger.error("NOTIF_CLIENT", "Error al conectar con Coral Notificaciones: " + e.getMessage(), e);

            throw new ServiceUnavailableException("No se pudo enviar el correo");
        }
    }

    /**
     * Envía una notificación junto con archivos adjuntos directamente al microservicio (Multipart).
     */
    public void enviarNotificacionConAdjuntos(SolicitudNotificacionDto solicitud, Map<String, byte[]> archivos) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 1. Agregar la solicitud como JSON
            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SolicitudNotificacionDto> solicitudPart = new HttpEntity<>(solicitud, jsonHeaders);
            body.add("solicitud", solicitudPart);

            // 2. Agregar los archivos adjuntos
            if (archivos != null && !archivos.isEmpty()) {
                for (Map.Entry<String, byte[]> entry : archivos.entrySet()) {
                    String nombreArchivo = entry.getKey();
                    ByteArrayResource fileResource = new ByteArrayResource(entry.getValue()) {
                        @Override
                        public String getFilename() {
                            return nombreArchivo;
                        }
                    };
                    HttpHeaders fileHeaders = new HttpHeaders();
                    fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);
                    body.add("archivos", filePart);
                }
            }

            ApiResponse<RespuestaNotificacionDto> response = restClient.post()
                    .uri("/api/v1/notificaciones/enviar-con-adjuntos")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.data() != null) {
                logger.info("NOTIF_CLIENT", "Notificación multipart encolada. TrackingId: {}", response.data().trackingId());
            }

        } catch (Exception e) {
            logger.error("NOTIF_CLIENT", "Error al conectar con Coral Notificaciones en endpoint multipart: " + e.getMessage(), e);
            throw new ServiceUnavailableException("No se pudo enviar el correo con adjuntos");
        }
    }
}