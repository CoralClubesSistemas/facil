package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;

public record DetalleReciboMovimientoDto(
        String descripcion,
        String referencia,
        BigDecimal importe,
        BigDecimal interes,
        BigDecimal iva,
        BigDecimal descuento,
        BigDecimal totalNeto
) {
}

