package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import org.springframework.jdbc.core.RowMapper;

public final class DashboardRowMappers {

    // patron singleton para evitar instanciación
    private DashboardRowMappers() {
    }

    public static <T> RowMapper<TotalCategoriaResponse<T>> crearMapper(Class<T> tipoId, String columnaConteo) {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getObject("id", tipoId),
                rs.getString("categoria"),
                rs.getObject(columnaConteo) != null ? rs.getInt(columnaConteo) : null,
                rs.getBigDecimal("total")
        );
    }

    public static <T> RowMapper<TotalCategoriaResponse<T>> crearMapper(Class<T> tipoId) {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getObject("id", tipoId),
                rs.getString("categoria"),
                rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null,
                rs.getBigDecimal("total")
        );
    }

    public static RowMapper<TotalCategoriaResponse<Long>> crearMapper() {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getObject("id", Long.class),
                rs.getString("categoria"),
                rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null,
                rs.getBigDecimal("total")
        );
    }
}
