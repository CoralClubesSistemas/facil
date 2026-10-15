package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
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
public class DashEstadisticasCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<IngresosTotalesResponse> ingresosTotalesMapper = (rs, rowNum) ->
            new IngresosTotalesResponse(rs.getBigDecimal("total"));

    private final RowMapper<EstadisticasTotalesResponse> estadisticasTotalesMapper = (rs, rowNum) ->
            new EstadisticasTotalesResponse(
                    rs.getObject("MES") != null ? rs.getInt("MES") : null,
                    rs.getString("MES_NOMBRE"),
                    rs.getBigDecimal("TOTAL")
            );

    public Optional<IngresosTotalesResponse> spDashBoardIngresosTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashBoardIngresosTotales", params, ingresosTotalesMapper);
    }

    public List<EstadisticasTotalesResponse> spDashEstadisticasTotales(Integer anio) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        return spExecutor.queryList("spDashEstadisticasTotales", params, estadisticasTotalesMapper);
    }

    public List<EstadisticasTotalesResponse> spDashEstadisticasTotalesXDesarrollo(Integer anio, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEstadisticasTotalesXDesarrollo", params, estadisticasTotalesMapper);
    }
}
