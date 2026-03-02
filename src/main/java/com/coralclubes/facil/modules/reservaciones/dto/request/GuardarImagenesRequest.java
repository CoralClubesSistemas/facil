package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Envoltorio para guardar las imágenes subidas de un hotel.
 */
public record GuardarImagenesRequest(
        @NotNull(message = "El ID es obligatorio")
        Integer id,

        @Valid
        List<ImagenRequest> imagenes
) {}