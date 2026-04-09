package com.coralclubes.facil.modules.cobranza.dto.response;

import java.util.UUID;

public record GenerarOrdenCobranzaResponse(
        Integer numeroOrden,
        Integer desarrolloId,
        UUID ordenUuid
) {
}

