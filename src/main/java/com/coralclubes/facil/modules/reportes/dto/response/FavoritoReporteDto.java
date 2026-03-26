package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record FavoritoReporteDto(
        Integer idFavorito,
        Integer idParametroFiltro,
        Integer valorSeleccionado
) {}
