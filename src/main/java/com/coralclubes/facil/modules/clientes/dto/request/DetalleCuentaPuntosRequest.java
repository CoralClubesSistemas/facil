package com.coralclubes.facil.modules.clientes.dto.request;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record DetalleCuentaPuntosRequest(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estatusPuntos,
        Integer numeroPlan
) {}
