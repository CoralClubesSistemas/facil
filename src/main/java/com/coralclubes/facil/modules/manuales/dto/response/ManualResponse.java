package com.coralclubes.facil.modules.manuales.dto.response;

import lombok.Builder;
import java.util.UUID;

@Builder
public record ManualResponse(
        Integer id,
        String nombre,
        String descripcion,
        Integer moduloId,
        String moduloNombre,
        Integer versionId,
        Integer version,
        UUID archivoUuid,
        String nombreArchivo,
        String tipo,
        String urlDescarga // URL temporal prefirmada generada en el Service
) {}
