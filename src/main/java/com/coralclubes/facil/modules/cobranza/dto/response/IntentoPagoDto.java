package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record IntentoPagoDto(
        Integer intentoPagoId,
        String formaPagoClave,
        String formaPagoDescripcion,
        String icono,
        String color,
        BigDecimal monto,
        String estatus,
        String metadata,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaAprobacion
) {}