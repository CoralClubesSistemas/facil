package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.AdendasDetallesResponse;
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
public class DashEventosCobranzaRepository {

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

    private final RowMapper<AdendasDetallesResponse> adendasDetallesMapper = (rs, rowNum) ->
            new AdendasDetallesResponse(
                    rs.getString("id"),
                    rs.getString("membresia"),
                    rs.getTimestamp("fechaVenta") != null ? rs.getTimestamp("fechaVenta").toLocalDateTime() : null,
                    rs.getTimestamp("fechaMovimiento") != null ? rs.getTimestamp("fechaMovimiento").toLocalDateTime() : null,
                    rs.getBigDecimal("importeCargo"),
                    rs.getBigDecimal("importePendiente"),
                    rs.getBigDecimal("totalBonificaciones"),
                    rs.getBigDecimal("totalPagos"),
                    rs.getString("ultimoRecibo"),
                    rs.getString("usuario"),
                    rs.getString("estatusMovimiento"),
                    rs.getString("tipoMovimiento"),
                    rs.getString("descripcionMovimiento")
            );

    public Optional<TotalCategoriaResponse<Long>> spDashEventosTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashEventosTotales", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashEventosDesarrollos(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashEventosDesarrollos", params, DashboardRowMappers.crearMapper());
    }

    public List<EventosDetallesResponse> spDashEventosDetalles(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEventosDetalles", params, eventosDetallesMapper);
    }

    public List<EventosOperadosResponse> spDashEventosAmortizados(String fechaInicio, String fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEventosAmortizados", params, eventosOperadosMapper);
    }

    public List<EventosDetallesResponse> spDashEventosAnticipos(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEventosAnticipos", params, eventosDetallesMapper);
    }

    public Optional<TotalCategoriaResponse<Long>> spDashEventosAnticiposTotal(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.querySingle("spDashEventosAnticiposTotal", params, DashboardRowMappers.crearMapper());
    }

    public List<EventosDetallesResponse> spDashEventosLiquidacion(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEventosLiquidacion", params, eventosDetallesMapper);
    }

    public Optional<TotalCategoriaResponse<Long>> spDashEventosLiquidacionTotal(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.querySingle("spDashEventosLiquidacionTotal", params, DashboardRowMappers.crearMapper());
    }

    public List<EventosOperadosResponse> spDashEventosOperados(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashEventosOperados", params, eventosOperadosMapper);
    }

    public List<TotalCategoriaResponse<Long>> spDashObtenerAdendasEventos(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashObtenerAdendasEventos", params, DashboardRowMappers.crearMapper());
    }

    public List<AdendasDetallesResponse> spDashObtenerAdendasEventosDetalles(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashObtenerAdendasEventosDetalles", params, adendasDetallesMapper);
    }
}
