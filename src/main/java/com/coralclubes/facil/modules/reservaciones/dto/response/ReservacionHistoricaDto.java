package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ReservacionHistoricaDto(
        Integer totalRegistros, // Dato de control para paginación
        String membresia,
        Integer consecutivo,
        String nombreDesarrollo,
        String nombreHuesped,
        String tipoUnidad,
        String numeroHabitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        LocalDateTime fechaRegistro,
        String estatusClave,
        String estatusDescripcion,
        BigDecimal importeTotal,
        BigDecimal importePendiente
) {}