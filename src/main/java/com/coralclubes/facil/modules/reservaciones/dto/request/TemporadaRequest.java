package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

@Builder
public record TemporadaRequest(
        Integer idTemporadaFecha,

        @NotEmpty(message = "Debe seleccionar al menos un desarrollo")
        List<Integer> idsDesarrollos,

        @NotNull(message = "El tipo de temporada es obligatorio")
        Integer idTipoTemporada,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha final es obligatoria")
        LocalDate fechaFinal
) {}