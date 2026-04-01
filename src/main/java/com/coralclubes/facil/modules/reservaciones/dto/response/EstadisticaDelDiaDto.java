package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

@Builder
public record EstadisticaDelDiaDto(
        String clave,
        String nombre,
        Integer valor,
        String adicional,
        String border,
        String icono,
        String text,
        String bg
) {}