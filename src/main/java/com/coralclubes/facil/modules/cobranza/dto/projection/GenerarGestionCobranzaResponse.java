package com.coralclubes.facil.modules.cobranza.dto.projection;

import java.util.UUID;

public record GenerarGestionCobranzaResponse(
        UUID tokenPagoEnLinea,
        Integer idGestionCobranza
) {
}

