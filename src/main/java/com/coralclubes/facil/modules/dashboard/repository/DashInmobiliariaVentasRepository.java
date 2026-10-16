package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosResponse;
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
public class DashInmobiliariaVentasRepository {

    private final StoredProcedureExecutor spExecutor;

    public DashInmobiliariaVentasRepository(@Qualifier("replicaStoredProcedureExecutor") StoredProcedureExecutor spExecutor) {
        this.spExecutor = spExecutor;
    }

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

    public List<TotalCategoriaResponse<Long>> spDashInmobiliariaClasificacionVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashInmobiliariaClasificacionVentas", params, DashboardRowMappers.crearMapper());
    }

    public List<InmobiliariaMovimientosResponse> spDashInmobiliariaCasasVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashInmobiliariaCasasVentas", params, inmobiliariaMovimientosMapper);
    }

    public List<InmobiliariaMovimientosResponse> spDashInmobiliariaTerrenosVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spDashInmobiliariaTerrenosVentas", params, inmobiliariaMovimientosMapper);
    }
}
