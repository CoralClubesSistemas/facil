package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DashGolfCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    public DashGolfCobranzaRepository(@Qualifier("replicaStoredProcedureExecutor") StoredProcedureExecutor spExecutor) {
        this.spExecutor = spExecutor;
    }

    public Optional<TotalCategoriaResponse<Long>> spDashGolfTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashGolfTotales", params, DashboardRowMappers.crearMapper());
    }

    public Optional<TotalCategoriaResponse<Long>> spDashGreenFeesTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashGreenFeesTotales", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashGreenFeesClasificacion(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashGreenFeesClasificacion", params, DashboardRowMappers.crearMapper());
    }

    public Optional<TotalCategoriaResponse<Long>> spDashOtrosIngresosGolf(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashOtrosIngresosGolf", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashOtrosIngresosGolfDetalles(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashOtrosIngresosGolfDetalles", params, DashboardRowMappers.crearMapper());
    }
}
