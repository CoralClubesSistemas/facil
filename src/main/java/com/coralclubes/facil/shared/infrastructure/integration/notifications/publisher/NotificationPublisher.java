package com.coralclubes.facil.shared.infrastructure.integration.notifications.publisher;

import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;

/**
 * Interfaz desacoplada para el envío de notificaciones de forma asíncrona mediante colas de mensajería.
 */
public interface NotificationPublisher {
    
    /**
     * Publica una solicitud de envío a la cola de entrada (INBOX) del microservicio.
     *
     * @param solicitud El DTO con los datos del envío.
     */
    void publicarSolicitud(SolicitudNotificacionDto solicitud);
}
