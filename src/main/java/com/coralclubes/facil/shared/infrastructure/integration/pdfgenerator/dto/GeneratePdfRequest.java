package com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record GeneratePdfRequest(
        String templateCode,
        Map<String, Object>data,
        StorageConfig storageConfig
) {
}
