package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record EstadoCumplimientoDto(
        boolean isCompletado,
        BigDecimal totalEsperado,
        BigDecimal totalAprobado,
        BigDecimal saldoPendiente,
        List<IntentoPagoDto> transacciones
) {}