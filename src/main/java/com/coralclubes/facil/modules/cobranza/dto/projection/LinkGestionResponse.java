package com.coralclubes.facil.modules.cobranza.dto.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LinkGestionResponse(
        String tokenUuid,
        Integer idGestion,
        LocalDate fechaVigencia
) {
}
