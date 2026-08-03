package com.coralclubes.facil.modules.clientes.dto.response;

import java.time.LocalDateTime;

public record CuponMembresiaResumenResponse(
        Integer id,
        String membresia,
        Integer idCupon,
        Integer movimientoGeneradorId,
        String movimientoGenerador,
        Integer cuponesOtorgados,
        Integer cuponesDisponibles,
        String estatus,
        LocalDateTime fechaOtorgado,
        String nombreCupon,
        String nomenclatura,
        String desarrollo,
        String origenCupon,
        Integer anioCupon,
        LocalDateTime inicioVigencia,
        LocalDateTime finVigencia
) {}
