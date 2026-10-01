package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.time.LocalDateTime;

public record CuponMembresiaReservacionDto(
        Integer idCupon,
        String nombreCupon,
        String descripcionCupon,
        String desarrollosAplicables,
        LocalDateTime inicioVigencia,
        LocalDateTime finVigencia,
        Integer cuponesDisponibles
) {}
