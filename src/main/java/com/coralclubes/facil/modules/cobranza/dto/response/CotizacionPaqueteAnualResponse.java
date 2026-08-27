package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CotizacionPaqueteAnualResponse(
        Integer paqueteAnualId,
        String membresia,
        Integer anio,
        Integer totalBeneficiariosActivos,
        BigDecimal porcentajeDescuentoAplicado,
        BigDecimal subtotalGeneral,
        BigDecimal descuentoGeneral,
        BigDecimal totalGeneral,
        List<PaqueteAnualDescuentoResponse> esquemasAplicados,
        List<CotizacionPaqueteAnualMovimientoResponse> movimientos
) {}
