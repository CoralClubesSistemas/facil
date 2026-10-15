package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosCobranzaResponse;
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
public class DashInmobiliariaCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<InmobiliariaMovimientosCobranzaResponse> movimientosCobranzaMapper = (rs, rowNum) ->
            new InmobiliariaMovimientosCobranzaResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("membresia"),
                    rs.getString("cliente"),
                    rs.getString("fecha"),
                    rs.getString("clasificacionMembresia"),
                    rs.getString("seccion"),
                    rs.getString("fraccionamiento"),
                    rs.getString("manzana"),
                    rs.getString("lote"),
                    rs.getBigDecimal("area"),
                    rs.getBigDecimal("precioMetro"),
                    rs.getBigDecimal("total")
            );

    public Optional<TotalCategoriaResponse<Long>> spDashInmobiliariaTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashInmobiliariaTotales", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashInmobiliariaClasificacion(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashInmobiliariaClasificacion", params, DashboardRowMappers.crearMapper());
    }

    public List<InmobiliariaMovimientosCobranzaResponse> spDashInmobiliariaCasas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashInmobiliariaCasas", params, movimientosCobranzaMapper);
    }

    public List<InmobiliariaMovimientosCobranzaResponse> spDashInmobiliariaTerrenos(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashInmobiliariaTerrenos", params, movimientosCobranzaMapper);
    }
}
