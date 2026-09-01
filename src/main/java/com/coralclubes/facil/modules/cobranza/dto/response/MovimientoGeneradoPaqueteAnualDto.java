package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MovimientoGeneradoPaqueteAnualDto(
        Integer mvtId,
        Integer tipoMovimientoId,
        String descripcion,
        BigDecimal cuota,
        BigDecimal montoDescuento,
        BigDecimal total,
        LocalDateTime fechaVencimiento
) {}
