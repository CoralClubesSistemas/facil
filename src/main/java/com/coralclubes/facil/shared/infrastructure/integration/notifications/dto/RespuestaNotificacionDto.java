package com.coralclubes.facil.shared.infrastructure.integration.notifications.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/* Respuesta de flujo sincrono (http) */
public record RespuestaNotificacionDto(
        UUID trackingId,
        String estatusInicial,
        LocalDateTime fechaRecepcion
) {}