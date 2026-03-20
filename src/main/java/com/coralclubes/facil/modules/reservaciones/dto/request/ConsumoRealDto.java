package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConsumoRealDto(
        @NotNull Integer idArticulo,
        @NotNull Integer sugerida,
        @NotNull Integer real,
        String observaciones
) {}