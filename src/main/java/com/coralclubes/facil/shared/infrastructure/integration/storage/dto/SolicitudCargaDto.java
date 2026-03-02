package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import lombok.Builder;
import java.util.Map;

/**
 * Petición que "Facil" le envía al Microservicio de Storage.
 */
@Builder
public record SolicitudCargaDto(
        String nombreArchivo,
        String contentType,
        Long tamanoBytes,
        String aliasConfiguracion,
        Map<String, String> metadatos,
        Boolean esPublico,
        String rutaLogica
) {}