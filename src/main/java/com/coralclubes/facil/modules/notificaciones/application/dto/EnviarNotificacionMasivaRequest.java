package com.coralclubes.facil.modules.notificaciones.application.dto;

import java.util.List;

public record EnviarNotificacionMasivaRequest(
        List<String> destinatarios,
        PeticionNotificacionDto contenido
) {}