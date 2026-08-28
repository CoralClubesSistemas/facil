package com.coralclubes.facil.modules.cobranza.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@Builder
public record ConfiguracionMovimientoDto(
        Integer movimiento,
        @JsonProperty("aplica_descuento")
        Boolean aplicaDescuento,
        Boolean obligatorio,
        @JsonProperty("configuracion_adicional")
        Map<String, Object> configuracionAdicional
) {}
