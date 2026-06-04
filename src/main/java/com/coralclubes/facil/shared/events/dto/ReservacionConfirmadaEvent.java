package com.coralclubes.facil.shared.events.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ReservacionConfirmadaEvent(
        String nombreReserva,
        String email,
        String email2,
        String peticionEspecial,
        String membresia,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String desarrollo,
        BigDecimal subtotal,
        List<Integer> foliosGenerados,
        List<HabitacionInfo> habitaciones
) {
    @Builder
    public record HabitacionInfo(
            String tipoHabitacion,
            Integer totalPersonas
    ) {}
}
