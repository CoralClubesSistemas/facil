package com.coralclubes.facil.shared.infrastructure.domain.dto;

import lombok.Builder;

/**
 * DTO para catálogos que provienen de LISTAS_VALORES pero
 * necesitan información adicional (Tabla y Clave).
 */
@Builder
public record CatalogoListasValores(
        Integer id,
        String descripcion,
        String tabla,
        String clave
) {}
