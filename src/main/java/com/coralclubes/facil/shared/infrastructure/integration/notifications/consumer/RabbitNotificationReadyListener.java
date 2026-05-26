package com.coralclubes.facil.shared.infrastructure.integration.notifications.consumer;

import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.NotificacionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Escucha las confirmaciones del microservicio de notificaciones en la cola READY.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitNotificationReadyListener {

    @RabbitListener(queues = "${app.rabbitmq.queues.notifications.ready}")
    public void recibirConfirmacion(NotificacionEvent confirmacion) {
        log.info("[RABBIT-LISTENER] Confirmación de notificación recibida - TrackingID: {} - Canal: {} - Estatus: {}",
                confirmacion.trackingId(), confirmacion.canal(), confirmacion.estatus());
        
        if ("FALLIDO".equalsIgnoreCase(confirmacion.estatus())) {
            log.error("[RABBIT-LISTENER] Fallo en el envío de notificación. Detalle: {}", confirmacion.detalle());
        }
    }
}
