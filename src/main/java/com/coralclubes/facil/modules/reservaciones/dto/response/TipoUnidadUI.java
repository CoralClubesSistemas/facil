package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record TipoUnidadUI(
        Integer idTipoUnidad,
        Integer idLsvTipoUnidad,
        String nombreTipoUnidad,
        Integer capacidad,
        String descripcionCorta,
        String urlImagen,
        BigDecimal calificacion,
        List<CaracteristicaDto> caracteristicas,
        Integer idDesarrollo,
        String nombreHotel
) {
}
