package com.coralclubes.facil.shared.infrastructure.notificaciones.application.dto;

import java.util.List;

public record EnviarNotificacionMasivaRequest(
        List<String> destinatarios,
        PeticionNotificacionDto contenido
) {}