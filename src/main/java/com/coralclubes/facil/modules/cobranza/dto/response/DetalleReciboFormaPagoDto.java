package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;

public record DetalleReciboFormaPagoDto(
        String tipoPago,
        BigDecimal importe,
        String terminacionTarjeta,
        String banco,
        String deposito
) {
}

