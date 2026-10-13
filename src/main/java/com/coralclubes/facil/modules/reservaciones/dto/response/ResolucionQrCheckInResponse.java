package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ResolucionQrCheckInResponse(
        String token,
        String membresia,
        List<DetalleReservacionDto> reservaciones
) {}
