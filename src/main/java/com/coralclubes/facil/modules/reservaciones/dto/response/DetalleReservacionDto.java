package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DetalleReservacionDto(
        String membresia,
        Integer consecutivo,
        Integer desarrolloId,
        String nombreDesarrollo,

        String nombreHuesped,
        boolean esSocio,

        Integer rhdtId,
        String tipoUnidad,
        Integer idUnidad,
        String numeroHabitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,

        LocalDateTime fechaHoraCheckIn,
        LocalDateTime fechaHoraCheckOut,

        String estatusClave,
        String estatusDescripcion,

        BigDecimal importeTotal,
        BigDecimal importePendiente,
        String ultimoReciboPagado,

        String promocionAplicada,
        Integer cuponPaqueteId,
        Integer puntosConsumidos,

        String peticionesEspeciales,

        List<CargoHabitacionDto> cargos,

        Integer numeroSocios,

        Integer cantidadTransferencias,
        Boolean haSidoTransferida,

        List<TransferenciaHabitacionDto> transferenciasHistorial
) {}