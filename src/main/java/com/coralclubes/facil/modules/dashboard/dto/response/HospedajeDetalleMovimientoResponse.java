package com.coralclubes.facil.modules.dashboard.dto.response;


public record HospedajeDetalleMovimientoResponse(
    Long id,
    String numUnidad,
    String tipoUnidad,
    String estatus,
    String membresia,
    String nombreReserva,
    String fechaReservacion,
    String fechaEntrada,
    String fechaSalida,
    String tiempoEstancia,
    Double importeReservacion,
    Double importePagado,
    String recibo
) {}
