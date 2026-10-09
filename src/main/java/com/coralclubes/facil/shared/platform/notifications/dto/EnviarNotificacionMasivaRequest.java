package com.coralclubes.facil.shared.platform.notifications.dto;

import java.util.List;

public record EnviarNotificacionMasivaRequest(
        List<String> destinatarios,
        PeticionNotificacionDto contenido
) {}
