package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

import java.util.Map;

@Builder
public record ConfiguracionMovimientoDto(
        Integer movimiento,
        Integer cantidad,
        Integer baseDeCobro,
        Map<String, Object> configuracion_adicional
) {}
