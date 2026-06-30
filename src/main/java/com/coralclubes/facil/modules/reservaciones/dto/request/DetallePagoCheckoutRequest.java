package com.coralclubes.facil.modules.reservaciones.dto.request;

import java.math.BigDecimal;

public record DetallePagoCheckoutRequest(
        Integer rrtId,
        Integer movimientoId,
        Integer personas,
        BigDecimal importeOriginal,
        BigDecimal descuento
) {}
