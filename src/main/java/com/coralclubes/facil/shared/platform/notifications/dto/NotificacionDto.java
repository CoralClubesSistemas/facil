package com.coralclubes.facil.shared.platform.notifications.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificacionDto(
        Long id,
        String remitente,
        String tipoMensaje,
        Integer nivelPrioridad,
        String titulo,
        String mensaje,
        Map<String, Object> metadata,
        LocalDateTime fechaCreacion,
        String estado
) {}
