package com.coralclubes.facil.modules.reservaciones.dto.request;

import java.math.BigDecimal;

public record DetalleCargoCheckoutRequest(
        Integer rrtId,
        Integer personas,
        BigDecimal importeOriginal,
        BigDecimal descuento,
        Integer idBeneficioAplicado,
        String motivoDescuento
) {}
