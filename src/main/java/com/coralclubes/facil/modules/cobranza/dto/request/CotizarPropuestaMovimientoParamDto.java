package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

import java.util.Map;

@Builder
public record CotizarPropuestaMovimientoParamDto(
        Integer movimientoId,
        Map<String, Object> configuracionAdicional
) {}
