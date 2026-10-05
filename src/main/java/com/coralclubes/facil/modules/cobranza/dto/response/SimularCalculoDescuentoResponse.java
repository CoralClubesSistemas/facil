package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record SimularCalculoDescuentoResponse(
        BigDecimal porcentajeRealAplicable,
        BigDecimal porcentajeAutorizado,
        Boolean requiereAutorizacion,
        Boolean autorizado,
        BigDecimal montoTotalOriginal,
        BigDecimal montoTotalDescuento,
        BigDecimal montoTotalConDescuento,
        BigDecimal montoTotalIva,
        BigDecimal montoTotalFinal,
        List<ItemCalculoDescuentoDto> items
) {
}
