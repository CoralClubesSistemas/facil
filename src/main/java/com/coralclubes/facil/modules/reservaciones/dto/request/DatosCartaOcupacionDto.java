package com.coralclubes.facil.modules.reservaciones.dto.request;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record DatosCartaOcupacionDto(
        String fechaEmision,
        String titular,
        String membresia,
        String foliosReservacion,
        List<HabitacionCartaDto> habitaciones,
        String observaciones,
        BigDecimal importeTotal,
        String fechaEntrada,
        String fechaSalida,
        String desarrollo
) {
    @Builder
    public record HabitacionCartaDto(
            String tipoHabitacion,
            Integer totalPax
    ) {}
}