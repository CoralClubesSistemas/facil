package com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator.dto;

import lombok.Builder;

@Builder
public record StorageConfig (
    String xApiKeyStorage,
    String aliasConfiguracion,
    String rutaLogica,
    String nombreArchivo
) {}

