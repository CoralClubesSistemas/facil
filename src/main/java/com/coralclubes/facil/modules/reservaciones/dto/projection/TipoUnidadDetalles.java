package com.coralclubes.facil.modules.reservaciones.dto.projection;

import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.shared.domain.dto.ImagenDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record TipoUnidadDetalles(
        Integer rhdtId,
        String nombreTipoUnidad,
        Integer capacidad,
        String descripcionCorta,
        String descripcionLarga,
        String nombreDesarrollo,
        BigDecimal calificacion,

        List<ImagenDto> imagenesUUID,
        List<CaracteristicaDto> caracteristicas

) {
}
