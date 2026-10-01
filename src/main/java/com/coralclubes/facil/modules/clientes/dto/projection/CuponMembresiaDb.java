package com.coralclubes.facil.modules.clientes.dto.projection;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CuponMembresiaDb(
        Integer id,
        String membresia,
        Integer idCupon,
        Integer movimientoGeneradorId,
        String movimientoGenerador,
        Integer cantidadCuponesTotales,
        Integer cuponesUsados,
        Integer cuponesDisponibles,
        String estatus,
        String nombreCupon,
        String descripcionCupon,
        String origenCuponId,
        String nomenclatura,
        String desarrollo,
        String origenCupon,
        Integer anioCupon,
        Integer anioVigencia,
        LocalDateTime fechaOtorgado,
        LocalDateTime inicioVigencia,
        LocalDateTime finVigencia,
        Boolean esTransferible,
        List<Integer> desarrollosAplicables,
        String desarrollosLegibles
) {
}
