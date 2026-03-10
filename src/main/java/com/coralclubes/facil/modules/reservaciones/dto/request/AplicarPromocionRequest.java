package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AplicarPromocionRequest (
        @NotNull(message = "El identificador del carrito es obligatorio") UUID groupId,
        @NotBlank(message = "El código de promoción no puede estar vacío") String codigoPromocion
) {}
