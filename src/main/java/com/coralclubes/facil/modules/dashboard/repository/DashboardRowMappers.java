package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import org.springframework.jdbc.core.RowMapper;

public final class DashboardRowMappers {

    private DashboardRowMappers() {}

    public static final RowMapper<TotalCategoriaResponse> TOTAL_CATEGORIA_MAPPER = (rs, rowNum) ->
            new TotalCategoriaResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("categoria"),
                    rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null,
                    rs.getBigDecimal("total")
            );
}
