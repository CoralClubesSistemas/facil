package com.coralclubes.facil.modules.reservaciones.dto.response;

import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.shared.infrastructure.domain.dto.ImagenDto;
import com.coralclubes.facil.shared.infrastructure.domain.dto.ImagenResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record TipoUnidadUIDetalles(
        Integer rhdtId,
        String nombreTipoUnidad,
        Integer capacidad,
        String descripcionCorta,
        String descripcionLarga,
        String nombreDesarrollo,
        BigDecimal calificacion,

        List<ImagenResponse> imagenes,
        List<CaracteristicaDto> caracteristicas

) {
}
