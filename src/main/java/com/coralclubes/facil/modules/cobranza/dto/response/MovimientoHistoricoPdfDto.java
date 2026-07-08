package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MovimientoHistoricoPdfDto(
        Long id,
        Integer familiaId,
        Integer padreId,
        String tipoMovimiento,
        LocalDateTime fechaGeneracion,
        LocalDateTime fechaVencimiento,
        BigDecimal importeCargo,
        BigDecimal importeAbono,
        BigDecimal importePendiente,
        BigDecimal interesMoratorio,
        String conceptoDescripcion,
        String descripcionMovimiento,
        String folioRecibo,
        LocalDateTime fechaPagoRecibo
) {
}
