package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecibosCancelados(
        String membresia,
        Integer numeroRecibo,
        Integer serieReciboId,
        String serieRecibo,
        String tipoRecibo,
        LocalDate fechaPago,
        BigDecimal importe,
        String estatusRecibo,
        String numeroTarjeta,
        BigDecimal saldoDisponible
) {
}
