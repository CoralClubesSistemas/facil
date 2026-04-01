package com.coralclubes.facil.shared.infrastructure.integration.notifications.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RespuestaNotificacionDto(
        UUID trackingId,
        String estatusInicial,
        LocalDateTime fechaRecepcion
) {}