package com.coralclubes.facil.shared.infrastructure.notificaciones.application.dto;

import java.util.Map;

public record PeticionNotificacionDto(
        String tipoMensaje,       // Ej: 'NUEVA_PROMOCION', 'SISTEMA_ACTUALIZACION'
        Integer nivelPrioridad,   // 1: Normal, 2: Alta
        String titulo,
        String mensaje,
        Map<String, Object> metadata // Se convertirá a JSON dinámicamente
) {}