package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

@Builder
public record MovimientoEstadoCuentaDto(
        String fecha,
        String fechaVencimiento,
        String concepto,
        String montoCargo,
        String montoInteres,
        String montoPendiente
) {
}
