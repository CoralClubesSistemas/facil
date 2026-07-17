package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RechazoCAResponse(
        String tarjeta,
        String membresia,
        String socio,
        LocalDateTime fechaRechazo,
        String motivoRechazo,
        String conceptoRechazo,
        BigDecimal importeRechazo,
        BigDecimal comisionCargo,
        LocalDateTime fechaRegistro,
        String usuarioRegistro
) {}
