package com.coralclubes.facil.shared.domain.dto;

import lombok.Builder;

import java.util.UUID;

// Dto que se devuelve al frontend (contiene la url directa)
@Builder
public record ImagenResponse (
    Integer idImagen,
    String urlImagen,
    UUID uuid,
    boolean esPortada,
    Integer orden
) {}
