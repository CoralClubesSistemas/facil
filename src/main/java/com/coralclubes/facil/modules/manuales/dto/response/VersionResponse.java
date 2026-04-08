package com.coralclubes.facil.modules.manuales.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record VersionResponse(
        Integer id,
        Integer version,
        String cambios,
        UUID archivoUuid,
        String nombreArchivo,
        String tipo,
        Boolean esActual,
        LocalDateTime fecha
) {}