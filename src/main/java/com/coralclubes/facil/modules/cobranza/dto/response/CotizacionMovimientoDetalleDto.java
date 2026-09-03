package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CotizacionMovimientoDetalleDto(
        String descripcion,
        BigDecimal cuota,
        LocalDateTime fechaVencimiento
) {}
