package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.EventosDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosOperadosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DashEventosVentasRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<EventosOperadosResponse> eventosOperadosMapper = (rs, rowNum) ->
            new EventosOperadosResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("membresia"),
                    rs.getString("desarrollo"),
                    rs.getString("cliente"),
                    rs.getString("fechaEvento"),
                    rs.getString("tipoEvento"),
                    rs.getString("totalPersonasEvento"),
                    rs.getBigDecimal("importeTotalEvento")
            );

    private final RowMapper<EventosDetallesResponse> eventosDetallesMapper = (rs, rowNum) ->
            new EventosDetallesResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("membresia"),
                    rs.getString("cliente"),
                    rs.getBigDecimal("total"),
                    rs.getString("fechaPago"),
                    rs.getString("fechaEvento"),
                    rs.getString("tipoEvento"),
                    rs.getString("recibo")
            );


    public List<TotalCategoriaResponse<Long>> spDashEventosDesarrollosVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashEventosDesarrollosVentas", params, DashboardRowMappers.crearMapper());
    }

    public List<EventosDetallesResponse> spDashEventosDetallesVentas(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEventosDetalles", params, eventosDetallesMapper);
    }

    public List<EventosOperadosResponse> spDashEventosVendidos(String fechaInicio, String fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEventosVendidos", params, eventosOperadosMapper);
    }
}
