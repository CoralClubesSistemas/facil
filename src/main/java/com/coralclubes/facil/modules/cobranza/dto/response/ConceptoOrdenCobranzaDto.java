package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;

public record ConceptoOrdenCobranzaDto(
        Integer idMovimiento,
        String concepto,
        String detalle,
        BigDecimal montoCapital,
        BigDecimal montoInteres,      // Cargo Total
        BigDecimal pagoInteres,       // NUEVO: Pago neto de interés
        BigDecimal interesesBonificados,
        BigDecimal montoIva,
        BigDecimal totalDescuento,
        BigDecimal subtotal
) {
}