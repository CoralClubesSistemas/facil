package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record OpcionPagoPuntosDto (
    Integer rrtId,
    Integer rhdtId,
    Integer promocionId,
    String nombrePromocion,
    Integer costoTotalPuntos
) {}