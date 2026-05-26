package com.coralclubes.facil.shared.infrastructure.integration.notifications.publisher;

import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publicador de solicitudes de notificación asíncronas para RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitNotificationPublisher implements NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.queues.notifications.inbox}")
    private String inboxQueue;

    @Value("${app.clients.notifications.api-key}")
    private String apiKey;

    @Override
    public void publicarSolicitud(SolicitudNotificacionDto solicitud) {
        log.info("[RABBIT-PUBLISHER] Enviando solicitud asíncrona a la cola INBOX: {}", inboxQueue);
        try {
            rabbitTemplate.convertAndSend(inboxQueue, solicitud, message -> {
                message.getMessageProperties().setHeader("x-api-key", apiKey);
                return message;
            });
        } catch (Exception e) {
            log.error("[RABBIT-PUBLISHER] Error al publicar en la cola de notificaciones", e);
            throw new RuntimeException("Error al encolar la solicitud de notificación", e);
        }
    }
}
