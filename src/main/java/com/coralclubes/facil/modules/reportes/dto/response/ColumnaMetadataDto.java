package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record ColumnaMetadataDto(
        String nombreColumnaDB,
        Integer ordenOriginal,
        String tipoDato
) {}
