package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record CotizacionMovimientoResponse(
        Integer tipoMovimientoId,
        String descripcion,
        Integer cantidadMovimientos,
        Integer baseDeCobroId,
        String baseDeCobro,
        Integer periodicidadId,
        String periodicidad,
        Integer totalBeneficiarios,
        BigDecimal tarifaUnitario,
        Integer anioVigenciaCuota,
        BigDecimal subtotal,
        List<CotizacionMovimientoDetalleDto> detalles,
        Map<String, Object> parametrosAplicados
) {}
