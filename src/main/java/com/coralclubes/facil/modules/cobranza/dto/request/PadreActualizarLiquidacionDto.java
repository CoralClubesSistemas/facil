package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PadreActualizarLiquidacionDto(
        Integer idMovimiento,
        BigDecimal montoAbonar,
        BigDecimal nuevoSaldoPendiente,
        Integer nuevoEstatusId
) {
}
