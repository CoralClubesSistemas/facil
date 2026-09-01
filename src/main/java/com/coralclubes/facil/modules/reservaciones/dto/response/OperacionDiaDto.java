package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record OperacionDiaDto(
        String membresia,
        Integer consecutivo,
        String nombreHuesped,
        boolean esSocio,
        Integer rhdtId,
        String tipoUnidad,
        String numeroHabitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String estatusReservacion,   // Ej: "CHECK-IN", "PENDIENTE"
        String descripcionEstatus,   // Ej: "Hospedado", "Por Llegar"
        BigDecimal importePendiente,
        String ultimoReciboPagado,
        Integer folioTarjeta
) {}