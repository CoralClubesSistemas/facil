package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosCobranzaResponse;
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
public class DashInmobiliariaCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    public DashInmobiliariaCobranzaRepository(@Qualifier("replicaStoredProcedureExecutor") StoredProcedureExecutor spExecutor) {
        this.spExecutor = spExecutor;
    }

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

    public Optional<TotalCategoriaResponse<Long>> spDashInmobiliariaTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.querySingle("spDashInmobiliariaTotales", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashInmobiliariaClasificacion(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashInmobiliariaClasificacion", params, DashboardRowMappers.crearMapper());
    }

    public List<InmobiliariaMovimientosCobranzaResponse> spDashInmobiliariaCasas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashInmobiliariaCasas", params, movimientosCobranzaMapper);
    }

    public List<InmobiliariaMovimientosCobranzaResponse> spDashInmobiliariaTerrenos(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashInmobiliariaTerrenos", params, movimientosCobranzaMapper);
    }
}
