package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.InventarioMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DashCarteraCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<InventarioMembresiasResponse> inventarioMembresiasMapper = (rs, rowNum) ->
            new InventarioMembresiasResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("desarrollo"),
                    rs.getString("cartera"),
                    rs.getString("membresia"),
                    rs.getString("cliente"),
                    rs.getString("tipoMembresia"),
                    rs.getBigDecimal("totalAdeudo")
            );

    public List<InventarioMembresiasResponse> spObtieneInventarioMembresiasTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spObtieneInventarioMembresiasTotales", params, inventarioMembresiasMapper);
    }

    public List<InventarioMembresiasResponse> spObtieneInventarioMembresiasDesarrollo(String fechaInicio, String fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spObtieneInventarioMembresiasDesarrollo", params, inventarioMembresiasMapper);
    }

    public List<TotalCategoriaResponse> spInventarioMembresiasTotalesGlobal(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spInventarioMembresiasTotalesGlobal", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public List<TotalCategoriaResponse> spObtieneInventarioMembresiasTotalesXCartera(String fechaInicio, String fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spObtieneInventarioMembresiasTotalesXCartera", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public List<TotalCategoriaResponse> spObtieneInventarioMembresiasTotalesXTipo(String fechaInicio, String fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spObtieneInventarioMembresiasTotalesXTipo", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }
}
