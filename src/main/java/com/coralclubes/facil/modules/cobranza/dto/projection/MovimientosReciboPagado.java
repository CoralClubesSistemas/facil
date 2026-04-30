package com.coralclubes.facil.modules.cobranza.dto.projection;

import java.math.BigDecimal;

public record MovimientosReciboPagado (
        Integer idMovimiento,
        Integer tipoMovimiento,
        BigDecimal montoPagado,
        Integer estatusId,
        String estatus
) {
}
