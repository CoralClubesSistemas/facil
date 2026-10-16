package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DashEstadisticasVentasRepository {

    private final StoredProcedureExecutor spExecutor;

    public DashEstadisticasVentasRepository(@Qualifier("replicaStoredProcedureExecutor") StoredProcedureExecutor spExecutor) {
        this.spExecutor = spExecutor;
    }

    private final RowMapper<IngresosTotalesResponse> ingresosTotalesMapper = (rs, rowNum) ->
            new IngresosTotalesResponse(rs.getBigDecimal("total"));

    private final RowMapper<EstadisticasTotalesResponse> estadisticasTotalesMapper = (rs, rowNum) ->
            new EstadisticasTotalesResponse(
                    rs.getObject("MES") != null ? rs.getInt("MES") : null,
                    rs.getString("MES_NOMBRE"),
                    rs.getBigDecimal("TOTAL")
            );

    public Optional<TotalCategoriaResponse<Long>> spDashMembresiasTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashMembresiasTotalesVentas", params, DashboardRowMappers.crearMapper());
    }

    public Optional<TotalCategoriaResponse<Long>> spDashGolfTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashGolfTotalesVentas", params, DashboardRowMappers.crearMapper());
    }

    public Optional<TotalCategoriaResponse<Long>> spDashEventosTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashEventosTotalesVentas", params, DashboardRowMappers.crearMapper());
    }

    public Optional<TotalCategoriaResponse<Long>> spDashInmobiliariaTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashInmobiliariaTotalesVentas", params, DashboardRowMappers.crearMapper());
    }

    public Optional<IngresosTotalesResponse> spDashBoardIngresosTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        params.put("desarrollo", desarrollo);
        return spExecutor.querySingle("spDashBoardIngresosTotalesVentas", params, ingresosTotalesMapper);
    }

    public List<EstadisticasTotalesResponse> spDashEstadisticasTotalesVentas(Integer anio) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        return spExecutor.queryList("spDashEstadisticasTotalesVentas", params, estadisticasTotalesMapper);
    }

    public List<EstadisticasTotalesResponse> spDashEstadisticasTotalesXDesarrolloVentas(Integer anio, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEstadisticasTotalesXDesarrolloVentas", params, estadisticasTotalesMapper);
    }
}
