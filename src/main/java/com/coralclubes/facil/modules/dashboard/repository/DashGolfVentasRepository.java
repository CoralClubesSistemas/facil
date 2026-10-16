package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesPromocionesXFechaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesXDiaResponse;
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
public class DashGolfVentasRepository {

    private final StoredProcedureExecutor spExecutor;

    public DashGolfVentasRepository(@Qualifier("replicaStoredProcedureExecutor") StoredProcedureExecutor spExecutor) {
        this.spExecutor = spExecutor;
    }

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

    public List<TotalCategoriaResponse<Long>> spDashGreenFeesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashGreenFeesVentas", params, DashboardRowMappers.crearMapper());
    }

    public List<GreenFeesXDiaResponse> spDashGreenFeesXDiaVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashGreenFeesXDiaVentas", params, greenFeesXDiaMapper);
    }

    public List<GreenFeesXDiaResponse> spDashGreenFeesXPteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashGreenFeesXPteVentas", params, greenFeesXDiaMapper);
    }

    public List<TotalCategoriaResponse<Long>> spDashGreenFeesPromociones(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashGreenFeesPromociones", params, DashboardRowMappers.crearMapper());
    }

    public List<DetallesMovimientosResponse> spDashGreenFeesPromocionesDetalles(LocalDate fechaInicio, LocalDate fechaFin, String promocion) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        params.put("promocion", promocion);
        return spExecutor.queryList("spDashGreenFeesPromocionesDetalles", params, detallesMovimientosMapper);
    }

    public List<GreenFeesPromocionesXFechaResponse> spDashGreenFeesPromocionesXFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashGreenFeesPromocionesXFecha", params, promocionesXFechaMapper);
    }

    public List<TotalCategoriaResponse<Long>> spDashTorneosPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashTorneosPeriodo", params, DashboardRowMappers.crearMapper());
    }

    public Optional<TotalCategoriaResponse<Long>> spDashTorneosTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashTorneosTotales", params, DashboardRowMappers.crearMapper());
    }
}
