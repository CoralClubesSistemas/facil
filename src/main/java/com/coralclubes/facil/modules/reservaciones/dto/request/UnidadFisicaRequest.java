package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UnidadFisicaRequest(
        Integer idUnidadFisica, // Null para crear
        @NotNull(message = "El ID del desarrollo es obligatorio")
        Integer idDesarrollo,
        Integer idTipoUnidad, // Opcional al crearla
        @NotBlank(message = "El número de unidad es obligatorio")
        String numeroUnidad,
        Integer piso,
        Integer idPadre // Opcional (Lock-off)
) {}