package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesPromocionesXFechaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesXDiaResponse;
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
public class DashGolfVentasRepository {

    private final StoredProcedureExecutor spExecutor;

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

    private final RowMapper<GreenFeesPromocionesXFechaResponse> promocionesXFechaMapper = (rs, rowNum) ->
            new GreenFeesPromocionesXFechaResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("movimiento"),
                    rs.getObject("numeroMovimientos") != null ? rs.getInt("numeroMovimientos") : null,
                    rs.getString("fecha")
            );

    private final RowMapper<GreenFeesXDiaResponse> greenFeesXDiaMapper = (rs, rowNum) ->
            new GreenFeesXDiaResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("categoria"),
                    rs.getString("membresia"),
                    rs.getString("cliente"),
                    rs.getString("fechaVenta"),
                    rs.getString("cantidad"),
                    rs.getString("total")
            );

    public Optional<TotalCategoriaResponse<Long>> spDashGolfTotalesVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashGolfTotalesVentas", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashGreenFeesVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashGreenFeesVentas", params, DashboardRowMappers.crearMapper());
    }

    public List<GreenFeesXDiaResponse> spDashGreenFeesXDiaVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashGreenFeesXDiaVentas", params, greenFeesXDiaMapper);
    }

    public List<GreenFeesXDiaResponse> spDashGreenFeesXPteVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashGreenFeesXPteVentas", params, greenFeesXDiaMapper);
    }

    public List<TotalCategoriaResponse<Long>> spDashGreenFeesPromociones(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashGreenFeesPromociones", params, DashboardRowMappers.crearMapper());
    }

    public List<DetallesMovimientosResponse> spDashGreenFeesPromocionesDetalles(String fechaInicio, String fechaFin, String promocion) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("promocion", promocion);
        return spExecutor.queryList("spDashGreenFeesPromocionesDetalles", params, detallesMovimientosMapper);
    }

    public List<GreenFeesPromocionesXFechaResponse> spDashGreenFeesPromocionesXFecha(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashGreenFeesPromocionesXFecha", params, promocionesXFechaMapper);
    }

    public List<TotalCategoriaResponse<Long>> spDashTorneosPeriodo(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashTorneosPeriodo", params, DashboardRowMappers.crearMapper());
    }

    public Optional<TotalCategoriaResponse<Long>> spDashTorneosTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashTorneosTotales", params, DashboardRowMappers.crearMapper());
    }
}
