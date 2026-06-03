package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record HotelesCardList(
        Integer id,
        String nombre,
        String descripcion,
        UUID portada_uuid,
        String portada_url
) {
}
