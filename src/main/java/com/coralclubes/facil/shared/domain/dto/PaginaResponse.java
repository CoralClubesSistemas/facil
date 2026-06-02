package com.coralclubes.facil.shared.domain.dto;

import java.util.List;

// DTO genérico para respuestas paginadas
public record PaginaResponse<T>(
        List<T> data,
        Integer totalRegistros,
        Integer paginaActual,
        Integer registrosPorPagina
) {}
