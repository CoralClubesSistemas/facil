package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MovimientoNuevoLiquidacionDto(
        String tipoRegistro,
        Integer raizMvtId,
        String padreRelacionTipo, // 'RAIZ', 'CARGO_INTERES', 'CARGO_IVA', 'CARGO_IVA_INT'
        BigDecimal importeCargo,
        BigDecimal importeAbono,
        Integer tipoMovId,
        Integer estatusId,
        String concepto
) {
}
