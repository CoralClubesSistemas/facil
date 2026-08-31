package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MovimientoManualResponse(
        String membresia,
        Integer mvtId,
        String descripcion,
        LocalDateTime fechaVencimiento,
        BigDecimal cuota
) {}
