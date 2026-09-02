package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record UltimoMovimientoResponse(
        String membresia,
        Integer idMovimiento,
        Integer numeroPlan,
        LocalDateTime fechaGeneracion,
        LocalDateTime fechaVencimiento,
        BigDecimal importeCargo,
        BigDecimal importeAbono,
        BigDecimal importePendiente,
        String usuarioGenera,
        Integer estatus,
        String estatusMovimiento,
        Integer numeroBeneficiarios,
        String concepto,
        String descripcion,
        Integer idTipoMovimiento,
        Integer periodicidadId,
        String periodicidad,
        Integer baseDeCobroId,
        String baseDeCobro,
        String tipoMovimiento
) {}
