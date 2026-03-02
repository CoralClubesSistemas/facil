package com.coralclubes.facil.modules.reservaciones.dto.response;

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
