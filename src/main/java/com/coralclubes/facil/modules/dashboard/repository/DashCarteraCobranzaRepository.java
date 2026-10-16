package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.InventarioMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DashCarteraCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    public DashCarteraCobranzaRepository(@Qualifier("replicaStoredProcedureExecutor") StoredProcedureExecutor spExecutor) {
        this.spExecutor = spExecutor;
    }

    private final RowMapper<InventarioMembresiasResponse> inventarioMembresiasMapper = (rs, rowNum) ->
            new InventarioMembresiasResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("desarrollo"),
                    rs.getString("cartera"),
                    rs.getString("membresia"),
                    rs.getString("cliente"),
                    rs.getString("tipoMembresia"),
                    rs.getBigDecimal("totalAdeudo")
            );

    public List<InventarioMembresiasResponse> spObtieneInventarioMembresiasTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spObtieneInventarioMembresiasTotales", params, inventarioMembresiasMapper);
    }

    public List<InventarioMembresiasResponse> spObtieneInventarioMembresiasDesarrollo(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spObtieneInventarioMembresiasDesarrollo", params, inventarioMembresiasMapper);
    }

    public List<TotalCategoriaResponse<Long>> spInventarioMembresiasTotalesGlobal(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        return spExecutor.queryList("spInventarioMembresiasTotalesGlobal", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spObtieneInventarioMembresiasTotalesXCartera(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spObtieneInventarioMembresiasTotalesXCartera", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spObtieneInventarioMembresiasTotalesXTipo(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", DashboardRowMappers.formatFecha(fechaInicio));
        params.put("fechaFin", DashboardRowMappers.formatFecha(fechaFin));
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spObtieneInventarioMembresiasTotalesXTipo", params, DashboardRowMappers.crearMapper());
    }
}
