package com.coralclubes.facil.shared.infrastructure.domain.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ImagenResponse (
    Integer idImagen,
    String urlImagen,
    UUID uuid,
    boolean esPortada,
    Integer orden
) {}
