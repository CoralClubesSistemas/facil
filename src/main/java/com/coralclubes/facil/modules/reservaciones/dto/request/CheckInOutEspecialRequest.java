package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckInOutEspecialRequest(
        @NotBlank String membresia,
        @NotNull Integer consecutivo,
        @NotNull TipoOperacionEspecial tipoOperacion
) {

    public enum TipoOperacionEspecial {
        CHECKIN,
        CHECKOUT
    }
}
