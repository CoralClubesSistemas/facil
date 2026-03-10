package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CampanaPuntosRequest(
        Integer idPromocion,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        String imagenUuid,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "La fecha de visibilidad es obligatoria")
        LocalDate fechaVisibilidad,

        @NotNull(message = "La temporada es obligatoria")
        Integer temporadaId,

        @NotNull(message = "El tabulador es obligatorio")
        List<TabuladorPuntosRequest> tabulador
) {
}