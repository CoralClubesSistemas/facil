package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DisponibilidadUnidadUI(
        Integer idTipoUnidad,
        String nombreUnidad,
        String descripcionCorta,
        Integer capacidad,
        Integer stockDisponible,
        BigDecimal costoEstancia,
        String urlImagen,
        List<CaracteristicaDto> caracteristicas
) {
}
