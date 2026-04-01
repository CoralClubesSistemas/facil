package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ConfirmarReservaRequest(
        @NotNull(message = "El identificador del carrito es obligatorio") UUID groupId,
        @NotBlank(message = "El nombre de la reserva es obligatorio") String nombreReserva,
        @NotBlank(message = "El email principal es obligatorio") String email,
        String email2,
        @NotBlank(message = "El teléfono es obligatorio") String telefono1,
        String telefono2,
        String peticionEspecial,
        List<Integer> totalPersonas, // Arreglo con la cantidad de personas por habitación [2, 2, 1]
        String codigoPromocion,
        CalcularCheckoutRequest.CuponRequest cupon,
        List<Integer> rrtIdsPagoPuntos
) {}