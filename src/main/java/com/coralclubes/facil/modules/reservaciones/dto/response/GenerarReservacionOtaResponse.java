package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

@Builder
public record GenerarReservacionOtaResponse(
        String membresia,
        Integer consecutivo,
        Integer idMovimiento
) {
}
