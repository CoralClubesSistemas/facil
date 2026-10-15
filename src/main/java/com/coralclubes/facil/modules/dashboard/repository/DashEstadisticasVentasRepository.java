package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
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
public class DashEstadisticasVentasRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<IngresosTotalesResponse> ingresosTotalesMapper = (rs, rowNum) ->
            new IngresosTotalesResponse(rs.getBigDecimal("total"));

    private final RowMapper<EstadisticasTotalesResponse> estadisticasTotalesMapper = (rs, rowNum) ->
            new EstadisticasTotalesResponse(
                    rs.getObject("MES") != null ? rs.getInt("MES") : null,
                    rs.getString("MES_NOMBRE"),
                    rs.getBigDecimal("TOTAL")
            );

    private final RowMapper<DetallesMovimientosResponse> detallesMovimientosMapper = (rs, rowNum) ->
            new DetallesMovimientosResponse(
                    rs.getObject("Id") != null ? rs.getLong("Id") : null,
                    rs.getObject("mvt_Id") != null ? rs.getInt("mvt_Id") : null,
                    rs.getString("Desarrollo"),
                    rs.getString("Membresia"),
                    rs.getString("Cliente"),
                    rs.getString("Movimiento"),
                    rs.getBigDecimal("Total"),
                    rs.getString("FechaDePago"),
                    rs.getString("NumeroRecibo")
            );

    public Optional<IngresosTotalesResponse> spDashBoardIngresosTotalesVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
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

    public List<DetallesMovimientosResponse> spDashDetallesMovimientos(String fechaInicio, String fechaFin, String desarrollo, String movimientos) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        params.put("movimientos", movimientos);
        return spExecutor.queryList("spDashDetallesMovimientos", params, detallesMovimientosMapper);
    }
}
