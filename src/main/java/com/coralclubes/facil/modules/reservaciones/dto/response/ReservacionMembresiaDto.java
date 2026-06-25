package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservacionMembresiaDto(
        String membresia,
        Integer folio,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        Integer personas,
        String nombreReserva,
        String emailContacto,
        String telefonoContacto,
        String estatusReservacion,
        Integer idTipoUnidad,
        String tipoUnidad,
        Integer capacidad,
        Integer idDesarrollo,
        String desarrollo,
        BigDecimal importeTotal,
        BigDecimal importePendiente
) {}
