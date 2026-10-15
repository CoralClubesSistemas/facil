package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetalleClienteResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarTotalesResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DashInmobiliariaCxcRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<InmobiliariaXCobrarTotalesResponse> totalesMapper = (rs, rowNum) ->
            new InmobiliariaXCobrarTotalesResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("clasificacion"),
                    rs.getBigDecimal("totalVenta"),
                    rs.getBigDecimal("totalPagado"),
                    rs.getBigDecimal("totalDeuda"),
                    rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null
            );

    private final RowMapper<InmobiliariaXCobrarDetallesResponse> detallesMapper = (rs, rowNum) ->
            new InmobiliariaXCobrarDetallesResponse(
                    rs.getString("membresia"),
                    rs.getString("cliente"),
                    rs.getString("fechaVenta"),
                    rs.getBigDecimal("intereses"),
                    rs.getBigDecimal("costoApertura"),
                    rs.getBigDecimal("totalVenta"),
                    rs.getBigDecimal("totalPagado"),
                    rs.getBigDecimal("totalDeuda")
            );

    private final RowMapper<InmobiliariaXCobrarDetalleClienteResponse> detalleClienteMapper = (rs, rowNum) ->
            new InmobiliariaXCobrarDetalleClienteResponse(
                    rs.getObject("id") != null ? rs.getLong("id") : null,
                    rs.getString("membresia"),
                    rs.getString("descripcion"),
                    rs.getString("fechaVencimiento"),
                    rs.getBigDecimal("importeCargo"),
                    rs.getBigDecimal("importeAbono"),
                    rs.getBigDecimal("importePendiente")
            );

    public List<InmobiliariaXCobrarTotalesResponse> spDashInmobiliariaXCobrarTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashInmobiliariaXCobrarTotales", params, totalesMapper);
    }

    public List<InmobiliariaXCobrarDetallesResponse> spDashInmobiliariaXCobrarDetalles(String fechaInicio, String fechaFin, String clasificacion) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("clasificacion", clasificacion);
        return spExecutor.queryList("spDashInmobiliariaXCobrarDetalles", params, detallesMapper);
    }

    public List<InmobiliariaXCobrarDetalleClienteResponse> spDashInmobiliariaXCobrarDetallesXCliente(String fechaInicio, String fechaFin, String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("membresia", membresia);
        return spExecutor.queryList("spDashInmobiliariaXCobrarDetallesXCliente", params, detalleClienteMapper);
    }
}
