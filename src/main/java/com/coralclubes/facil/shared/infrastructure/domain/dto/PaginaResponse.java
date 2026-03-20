package com.coralclubes.facil.shared.infrastructure.domain.dto;

import java.util.List;

public record PaginaResponse<T>(
        List<T> data,
        Integer totalRegistros,
        Integer paginaActual,
        Integer registrosPorPagina
) {}