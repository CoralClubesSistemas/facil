package com.coralclubes.facil.modules.reservaciones.dto.request;

import java.math.BigDecimal;

public record DetalleReservacionJson(
        Integer rrtId,
        Integer personas,
        BigDecimal importeOriginal,
        BigDecimal descuento,
        String observacionPagoPuntos
) {}