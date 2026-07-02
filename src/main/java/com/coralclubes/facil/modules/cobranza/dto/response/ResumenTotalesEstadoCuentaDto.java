package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

@Builder
public record ResumenTotalesEstadoCuentaDto(
        String totalCargos,
        String totalIntereses,
        String totalNetoExigible
) {
}
