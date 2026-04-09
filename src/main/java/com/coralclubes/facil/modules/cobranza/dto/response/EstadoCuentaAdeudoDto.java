package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EstadoCuentaAdeudoDto(
        Integer id,
        Integer movimientoOriginalId,
        Integer movimientoPadreId,
        LocalDateTime fechaGeneracion,
        LocalDateTime fechaVencimiento,
        Integer diasAtraso,
        BigDecimal importeCargo,
        BigDecimal importeAbono,
        BigDecimal importePendiente,
        BigDecimal interesMoratorio,
        BigDecimal totalAPagar,
        String concepto,
        String detalle,
        String tipoMovimiento,
        String estatusNombre,
        String desarrolloNombre,
        Integer desarrolloId,
        String usuarioCaptura
) {
}

