package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CalcularCheckoutRequest(
        @NotNull(message = "El identificador del carrito es obligatorio") UUID groupId,
        String codigoPromocion,
        Integer cuponId,
        List<Integer> rrtIdsPagoPuntos
) {}