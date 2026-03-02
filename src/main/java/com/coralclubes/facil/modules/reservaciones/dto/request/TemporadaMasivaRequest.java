package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.time.LocalDate;

@Builder
public record TemporadaMasivaRequest(
        @NotNull Integer idDesarrollo,
        @NotNull Integer idTipoTemporada,
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFinal
) {}