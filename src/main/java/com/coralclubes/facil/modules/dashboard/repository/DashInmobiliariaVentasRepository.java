package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosResponse;
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
public class DashInmobiliariaVentasRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<InmobiliariaMovimientosResponse> inmobiliariaMovimientosMapper = (rs, rowNum) ->
            new InmobiliariaMovimientosResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("membresia"),
                    rs.getString("cliente"),
                    rs.getString("fecha"),
                    rs.getString("clasificacionMembresia"),
                    rs.getString("seccion"),
                    rs.getString("fraccionamiento"),
                    rs.getString("manzana"),
                    rs.getString("lote"),
                    rs.getString("area"),
                    rs.getString("precioMetro"),
                    rs.getString("total"),
                    rs.getString("enganche"),
                    rs.getString("montoProcesable"),
                    rs.getString("fechaProcesable"),
                    rs.getString("montoPagado")
            );

    public Optional<TotalCategoriaResponse> spDashInmobiliariaTotalesVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashInmobiliariaTotalesVentas", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public List<TotalCategoriaResponse> spDashInmobiliariaClasificacionVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashInmobiliariaClasificacionVentas", params, DashboardRowMappers.TOTAL_CATEGORIA_MAPPER);
    }

    public List<InmobiliariaMovimientosResponse> spDashInmobiliariaCasasVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashInmobiliariaCasasVentas", params, inmobiliariaMovimientosMapper);
    }

    public List<InmobiliariaMovimientosResponse> spDashInmobiliariaTerrenosVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashInmobiliariaTerrenosVentas", params, inmobiliariaMovimientosMapper);
    }
}
