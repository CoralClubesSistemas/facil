package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TemporadaPeriodoResponse(
        LocalDate fecha,
        Integer temporadaId,
        String temporadaNombre,
        LocalDate temporadaInicio,
        LocalDate temporadaFin
) {}
