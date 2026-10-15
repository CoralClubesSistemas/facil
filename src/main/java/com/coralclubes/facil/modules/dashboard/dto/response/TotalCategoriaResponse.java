package com.coralclubes.facil.modules.dashboard.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Dto General de respuesta para estadisticas de total por categoria
 *
 * @param <T>            Tipo de dato del id de la categoria
 * @param id             Identificador de la categoria
 * @param categoria      Nombre de la categoria
 * @param totalElementos Total de elementos de la categoria
 * @param total          Importe total
 * @param metadata       Metadatos adicionales
 */
@Builder
public record TotalCategoriaResponse<T>(
        T id,
        String categoria,
        Integer totalElementos,
        BigDecimal total,
        Map<String, Object> metadata
) {
    /**
     * Constructor adicional sin metadata.
     */
    public TotalCategoriaResponse(
            T id,
            String categoria,
            Integer totalElementos,
            BigDecimal total
    ) {
        this(id, categoria, totalElementos, total, null);
    }
}
