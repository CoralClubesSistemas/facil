package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record HistoricoMovimientosRequest(
        @NotBlank String membresia,
        List<String> tipoMovimientos,
        Integer estatusMovimientos,
        Integer desarrolloConsumo,
        Integer idPadre
) {
}
