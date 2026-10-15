package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.*;
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
public class DashMembresiasVentasRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<MovimientoVentaXDesarrolloResponse> movimientoVentaXDesarrolloMapper = (rs, rowNum) ->
            new MovimientoVentaXDesarrolloResponse(
                    rs.getString("movimiento"),
                    rs.getBigDecimal("total")
            );

    private final RowMapper<MembresiasProcesablesDetallesResponse> procesablesDetallesMapper = (rs, rowNum) ->
            new MembresiasProcesablesDetallesResponse(
                    rs.getString("Membresia"),
                    rs.getString("TipoMembresia"),
                    rs.getString("NombreCliente"),
                    rs.getString("FechaVenta"),
                    rs.getString("Procedencia"),
                    rs.getBigDecimal("ImporteVenta"),
                    rs.getBigDecimal("EnganchePactado"),
                    rs.getBigDecimal("MontoProcesable"),
                    rs.getString("FechaProcesable"),
                    rs.getBigDecimal("MontoPagado"),
                    rs.getObject("NumeroMensualidades") != null ? rs.getInt("NumeroMensualidades") : null,
                    rs.getString("EstatusVenta")
            );

    private final RowMapper<MejoresClientesResponse> mejoresClientesMapper = (rs, rowNum) ->
            new MejoresClientesResponse(
                    rs.getString("membresia"),
                    rs.getString("nombre"),
                    rs.getObject("total") != null ? rs.getInt("total") : null
            );

    private final RowMapper<UltimosClientesResponse> ultimosClientesMapper = (rs, rowNum) ->
            new UltimosClientesResponse(
                    rs.getString("membresia"),
                    rs.getString("primerNombre"),
                    rs.getString("segundoNombre"),
                    rs.getString("apellidoPaterno"),
                    rs.getString("apellidoMaterno"),
                    rs.getString("desarrollo"),
                    rs.getString("fecha")
            );

    private final RowMapper<UltimasMembresiasResponse> ultimasMembresiasMapper = (rs, rowNum) ->
            new UltimasMembresiasResponse(
                    rs.getString("desarrollo"),
                    rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null,
                    rs.getString("fechaInicio"),
                    rs.getString("fechaActual")
            );

    private final RowMapper<EstatusMembresiasResponse> estatusMembresiasMapper = (rs, rowNum) ->
            new EstatusMembresiasResponse(
                    rs.getString("estatus"),
                    rs.getObject("total") != null ? rs.getInt("total") : null
            );

    public Optional<TotalCategoriaResponse<Long>> spDashMembresiasTotalesVentas(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashMembresiasTotalesVentas", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashMembresiasDesarrollos(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashMembresiasDesarrollos", params, DashboardRowMappers.crearMapper());
    }

    public List<MovimientoVentaXDesarrolloResponse> spDashMovimientosVentasXDesarrollo(String fechaInicio, String fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashMovimientosVentasXDesarrollo", params, movimientoVentaXDesarrolloMapper);
    }

    public List<TotalCategoriaResponse<Long>> spDashMembresiasProcesables(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashMembresiasProcesables", params, DashboardRowMappers.crearMapper());
    }

    public List<MembresiasProcesablesDetallesResponse> spDashMembresiasProcesablesDetalles(String fechaInicio, String fechaFin, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashMembresiasProcesablesDetalles", params, procesablesDetallesMapper);
    }

    public List<MejoresClientesResponse> spDashMejoresClientes(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashMejoresClientes", params, mejoresClientesMapper);
    }

    public List<UltimosClientesResponse> spDashUltimosClientes(Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashUltimosClientes", params, ultimosClientesMapper);
    }

    public List<UltimasMembresiasResponse> spDashUltimasMembresias() {
        return spExecutor.queryList("spDashUltimasMembresias", Map.of(), ultimasMembresiasMapper);
    }

    public List<EstatusMembresiasResponse> spDashEstatusMembresias(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashEstatusMembresias", params, estatusMembresiasMapper);
    }
}
