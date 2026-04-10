package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;

public record ConceptoOrdenCobranzaDto(
        Integer idMovimiento,
        String concepto,
        String detalle,
        BigDecimal montoCapital,
        BigDecimal montoInteres,
        BigDecimal interesesBonificados,
        BigDecimal montoIva,
        BigDecimal totalDescuento,
        BigDecimal subtotal
) {
}

