package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TipoUnidadRequest(
        Integer idTipoUnidad, // Null para crear, valor para actualizar
        Integer idLsvTipoUnidad, // Null si se va a crear un nuevo nombre

        @Size(max = 200, message = "El nombre no puede exceder los 200 caracteres")
        String nombreTipoUnidad, // Requerido si idLsvTipoUnidad es nulo

        @NotNull(message = "El ID del desarrollo (Hotel) es obligatorio")
        Integer idDesarrollo,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser al menos de 1 persona")
        Integer capacidad,

        String descripcionCorta,
        String descripcionLarga
) {}