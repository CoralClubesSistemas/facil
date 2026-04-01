package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CargoHabitacionDto(
        Integer idMovimiento,
        String descripcion,
        BigDecimal importeCargo,
        BigDecimal importePendiente,
        LocalDateTime fechaRegistro
) {}