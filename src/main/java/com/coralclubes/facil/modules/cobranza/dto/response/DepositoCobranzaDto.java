package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositoCobranzaDto(
        Integer idDeposito,
        LocalDate fechaOperacion,
        String concepto,
        String referencia,
        String referenciaAmpliada,
        BigDecimal importeDeposito,
        BigDecimal importeDisponible,
        String banco
) {
}

