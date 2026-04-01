package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record TemporadaDto(
        Integer idTemporadaFecha,
        Integer idDesarrollo,
        String nombreDesarrollo,
        Integer idTipoTemporada,
        String nombreTemporada,
        LocalDate fechaInicio,
        LocalDate fechaFinal
) {}