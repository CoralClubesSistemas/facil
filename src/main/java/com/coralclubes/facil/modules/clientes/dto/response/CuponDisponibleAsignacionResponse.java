package com.coralclubes.facil.modules.clientes.dto.response;

import java.time.LocalDateTime;

public record CuponDisponibleAsignacionResponse(
        Integer cuponId,
        String nombreCupon,
        String nomenclatura,
        Integer anio,
        LocalDateTime inicioVigencia,
        LocalDateTime finVigencia,
        Boolean esTransferible,
        Integer origenId,
        String origenNombre,
        Integer cantidadCupones,
        String nombrePeriodo,
        LocalDateTime fechaInicioPeriodo,
        LocalDateTime fechaFinPeriodo
) {}
