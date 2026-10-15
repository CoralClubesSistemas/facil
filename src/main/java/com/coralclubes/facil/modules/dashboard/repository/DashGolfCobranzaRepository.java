package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DashGolfCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    public Optional<TotalCategoriaResponse> spDashGolfTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashGolfTotales", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public Optional<TotalCategoriaResponse> spDashGreenFeesTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashGreenFeesTotales", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public List<TotalCategoriaResponse> spDashGreenFeesClasificacion(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashGreenFeesClasificacion", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public Optional<TotalCategoriaResponse> spDashOtrosIngresosGolf(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashOtrosIngresosGolf", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public List<TotalCategoriaResponse> spDashOtrosIngresosGolfDetalles(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashOtrosIngresosGolfDetalles", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }
}
