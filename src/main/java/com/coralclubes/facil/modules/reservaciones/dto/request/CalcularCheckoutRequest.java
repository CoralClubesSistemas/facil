package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CalcularCheckoutRequest(
        @NotNull(message = "El identificador del carrito es obligatorio") UUID groupId,
        String codigoPromocion,
        CuponRequest cupon,
        List<Integer> rrtIdsPagoPuntos
) {
    public record CuponRequest(
            String tipoDescuento,
            Integer paqueteId,
            Integer consecutivo,
            BigDecimal porcentajeDescuento
    ) {}
}