package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MembresiaDetalleProcesableDto(
        Integer numeroPlan,
        BigDecimal montoProcesable,
        LocalDateTime fechaProcesable,
        BigDecimal montoBonificado,
        BigDecimal montoPagado,
        String estatusProcesable
) {
}
