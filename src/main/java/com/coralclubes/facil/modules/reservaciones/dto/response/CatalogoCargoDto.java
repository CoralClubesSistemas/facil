package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.math.BigDecimal;

public record CatalogoCargoDto(
        Integer tipoMovimientoId,
        String descripcion,
        BigDecimal cuota
) {}