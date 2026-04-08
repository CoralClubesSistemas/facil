package com.coralclubes.facil.modules.manuales.dto.response;

import lombok.Builder;

@Builder
public record ManualResponse(
        Integer totalRegistros,
        Integer id,
        String nombre,
        String descripcion,
        Integer moduloId,
        Integer moduloPadreId,
        String moduloPadreNombre,
        String moduloNombre,
        Integer version,
        String tipo
) {}