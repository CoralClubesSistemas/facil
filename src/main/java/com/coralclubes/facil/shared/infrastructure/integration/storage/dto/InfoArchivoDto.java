package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

// Para consultar detalles de un archivo ya existente.
public record InfoArchivoDto(
        UUID uuid,
        String nombreOriginal,
        String extension,
        String contentType,
        Long tamanoBytes,
        String estatus,        // DISPONIBLE, ERROR, etc.
        Boolean esPublico,
        String urlDescarga,    // URL final (firmada o pública)
        OffsetDateTime fechaCreacion
) {}