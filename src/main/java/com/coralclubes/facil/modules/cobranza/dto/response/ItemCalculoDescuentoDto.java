package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ItemCalculoDescuentoDto(
        Integer idMovimiento,
        BigDecimal montoCapitalOriginal,
        BigDecimal porcentajeAplicado,
        BigDecimal montoDescuento,
        BigDecimal montoCapitalConDescuento,
        BigDecimal pagoInteresOriginal,
        BigDecimal interesesBonificados,
        BigDecimal montoIva,
        BigDecimal montoIvaInteres,
        BigDecimal totalPagarItem
) {
}
