package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

@Builder
public record OpcionPagoPuntosDto (
    Integer rrtId,
    Integer rhdtId,
    Integer promocionId,
    String nombrePromocion,
    Integer costoTotalPuntos
) {}