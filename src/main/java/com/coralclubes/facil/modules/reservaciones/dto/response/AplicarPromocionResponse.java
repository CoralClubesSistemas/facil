package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.math.BigDecimal;

public record AplicarPromocionResponse (
        BigDecimal montoOriginal,
        BigDecimal montoDescuento,
        BigDecimal montoFinal,
        boolean esValido,
        String mensajeRetorno
) {}