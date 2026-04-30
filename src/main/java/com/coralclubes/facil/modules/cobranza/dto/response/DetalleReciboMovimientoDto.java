package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;

public record DetalleReciboMovimientoDto(
        Integer idMovimiento,
        Integer tipoMovimiento,
        String descripcion,
        String referencia,
        BigDecimal importe,
        BigDecimal interes,
        BigDecimal iva,
        BigDecimal descuento,
        BigDecimal totalNeto
) {
}

