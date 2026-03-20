package com.coralclubes.facil.modules.reservaciones.dto.request;

import java.time.LocalDate;

public record DesactivarUnidadRequest(
        Integer idUnidadFisica,
        String razonBloqueo,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
