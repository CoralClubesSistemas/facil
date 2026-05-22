package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import lombok.Builder;
import java.util.Map;

/**
 * Petición de carga síncrona para el microservicio de almacenamiento (flujo legacy).
 */
@Builder
public record SolicitudCargaLegacyDto(
        String idCorrelacion,
        String aliasConfiguracion,
        Map<String, String> metadatos,
        Boolean esPublico,
        String rutaLogica,
        Boolean requiereDepuracion
) {}
