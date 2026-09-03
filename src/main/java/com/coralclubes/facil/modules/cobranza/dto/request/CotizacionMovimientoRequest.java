package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record CotizacionMovimientoRequest(
        String membresia,
        Integer tipoMovimientoId,
        LocalDate fechaVencimiento,
        Integer desarrolloConsumo,
        Map<String, Object> parametrosEspeciales
) {}
