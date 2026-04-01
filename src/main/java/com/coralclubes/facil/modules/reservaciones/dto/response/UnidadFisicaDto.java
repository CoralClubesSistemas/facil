package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

@Builder
public record UnidadFisicaDto(
        Integer idUnidadFisica,
        String numeroUnidad,
        Integer piso,
        Integer idPadre,
        String numeroPadre,
        Integer idTipoUnidad,
        String nombreTipoUnidad,
        Integer idDesarrollo,
        String nombreDesarrollo
) {}