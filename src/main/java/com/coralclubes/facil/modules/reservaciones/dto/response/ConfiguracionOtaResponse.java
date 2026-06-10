package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ConfiguracionOtaResponse(
        Integer idConfiguracionOta,
        Integer lsvOta,
        String nombreOta,
        Integer idDesarrollo,
        String nombreDesarrollo,
        BigDecimal porcentajeComision,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
