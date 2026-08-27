package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MovimientoPaqueteAnualResponse(
        Integer id,
        String descripcion,
        String periodicidad,
        Integer baseDeCobroId,
        String baseDeCobro,
        BigDecimal cuota,
        Integer anioVigencia
) {}
