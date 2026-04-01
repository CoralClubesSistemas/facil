package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EnlazarImagenRequest(
        @NotNull(message = "El ID de la promoción es obligatorio") Integer idPromocion,
        @NotNull(message = "El UUID de la imagen es obligatorio") UUID uuidImagen
) {}