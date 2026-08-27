package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record CotizacionPaqueteAnualMovimientoResponse(
        Integer paqueteAnualMovimientoId,
        Integer movimientoId,
        String movimiento,
        Integer cantidadMovimientos,
        Integer baseDeCobroId,
        String baseDeCobro,
        String periodicidad,
        Integer totalBeneficiarios,
        Boolean aplicaDescuento,
        Boolean obligatorio,
        BigDecimal tarifaUnitario,
        Integer anioVigenciaCuota,
        BigDecimal subtotal,
        BigDecimal montoDescuento,
        BigDecimal total,
        Map<String, Object> configuracionAdicional
) {}
