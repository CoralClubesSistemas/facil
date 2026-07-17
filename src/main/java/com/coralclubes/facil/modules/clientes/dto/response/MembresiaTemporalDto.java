package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaTemporalDto(
        String desarrollo,
        String estatusMembresia,
        String membresia,
        String socioTemporal,
        LocalDateTime fechaVenta
) {
}
