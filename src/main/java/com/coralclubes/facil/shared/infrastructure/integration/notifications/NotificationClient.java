package com.coralclubes.facil.shared.infrastructure.integration.notifications;

import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.RespuestaNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
}