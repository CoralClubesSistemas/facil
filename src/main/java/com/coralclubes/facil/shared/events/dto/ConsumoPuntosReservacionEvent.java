package com.coralclubes.facil.shared.events.dto;

import lombok.Builder;

@Builder
public record ConsumoPuntosReservacionEvent(
        String membresia,
        Integer desarrolloId,
        Integer totalPuntos,
        Integer idMovimiento,
        String descripcion,
        String usuario
) {}
