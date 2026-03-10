package com.coralclubes.facil.shared.infrastructure.domain.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * Proyección de sps que devuelven las imagenes de los hoteles o las habitaciones.
 */
@Builder
public record ImagenDto(
        Integer idImagen,
        UUID uuid,
        boolean esPortada,
        Integer orden
) {}