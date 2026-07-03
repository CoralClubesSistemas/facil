package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

@Builder
public record SintetizarCuerpoCorreoRequest(
        boolean nombre,
        boolean desarrollo,
        boolean membresia,
        boolean correo,
        boolean totalAdeudo,
        boolean intereses,
        boolean convenioCie
) {
}
