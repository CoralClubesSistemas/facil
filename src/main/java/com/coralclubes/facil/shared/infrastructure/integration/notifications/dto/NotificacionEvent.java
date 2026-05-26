package com.coralclubes.facil.shared.infrastructure.integration.notifications.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de confirmación recibido de la cola READY de Coral Notificaciones.
 */
public record NotificacionEvent(
        UUID trackingId,
        String codigoSistema,
        String canal,
        String proveedor,
        String destinatario,
        String estatus,
        String detalle,
        LocalDateTime fechaProcesamiento,
        Map<String, Object> metadatos
) implements Serializable {
}
