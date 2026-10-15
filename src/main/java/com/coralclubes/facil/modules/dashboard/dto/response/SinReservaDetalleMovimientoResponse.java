package com.coralclubes.facil.modules.dashboard.dto.response;


public record SinReservaDetalleMovimientoResponse(
    Integer id,
    Integer movimientoId,
    String desarrollo,
    String membresia,
    String cliente,
    String movimiento,
    Double total,
    String fechaPago,
    Integer numeroRecibo
) {}
