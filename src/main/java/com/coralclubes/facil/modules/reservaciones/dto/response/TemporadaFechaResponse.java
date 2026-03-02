package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TemporadaFechaResponse(
        Integer idDesarrollo,
        String nombreDesarrollo,
        Integer idTemporada,
        String nombreTemporada,
        LocalDate fechaInicio,
        LocalDate fechaFinal
) {}
