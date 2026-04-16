package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;

public record FinalizarOrdenCobranzaResponse(
        Integer numeroRecibo,
        Integer serieReciboId,
        BigDecimal totalPagado
) {
}

