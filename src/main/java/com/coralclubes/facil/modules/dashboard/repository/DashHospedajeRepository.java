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
public class DashHospedajeRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<HospedajeLibreResponse> hospedajeLibreMapper = (rs, rowNum) ->
            new HospedajeLibreResponse(
                    rs.getString("Unidad"),
                    rs.getObject("CantidadLibres") != null ? rs.getInt("CantidadLibres") : null
            );

    private final RowMapper<HospedajesDisponiblesResponse> disponibildadMapper = (rs, rowNum) ->
            new HospedajesDisponiblesResponse(
                    rs.getObject("Dia") != null ? rs.getLong("Dia") : null,
                    rs.getString("EtiquetaUDisponibles")
            );

    private final RowMapper<HospedajeDetalleMovimientoResponse> detalleMovimientoMapper = (rs, rowNum) ->
            new HospedajeDetalleMovimientoResponse(
                    rs.getObject("Id") != null ? rs.getLong("Id") : null,
                    rs.getString("NumUnidad"),
                    rs.getString("TipoUnidad"),
                    rs.getString("Estatus"),
                    rs.getString("Membresia"),
                    rs.getString("NombreReserva"),
                    rs.getString("FechaReservacion"),
                    rs.getString("FechaEntrada"),
                    rs.getString("FechaSalida"),
                    rs.getString("TiempoEstancia"),
                    rs.getObject("ImporteReservacion") != null ? rs.getDouble("ImporteReservacion") : null,
                    rs.getObject("ImportePagado") != null ? rs.getDouble("ImportePagado") : null,
                    rs.getString("Recibo")
            );

    private final RowMapper<ReservasCanceladasResponse> reservasCanceladasMapper = (rs, rowNum) ->
            new ReservasCanceladasResponse(
                    rs.getString("tipoUnidad"),
                    rs.getObject("cantidad") != null ? rs.getInt("cantidad") : null
            );

    private final RowMapper<SinReservaDetalleMovimientoResponse> sinReservaMapper = (rs, rowNum) ->
            new SinReservaDetalleMovimientoResponse(
                    rs.getObject("Id") != null ? rs.getInt("Id") : null,
                    rs.getObject("movimiento_Id") != null ? rs.getInt("movimiento_Id") : null,
                    rs.getString("Desarrollo"),
                    rs.getString("Membresia"),
                    rs.getString("Cliente"),
                    rs.getString("Movimiento"),
                    rs.getObject("Total") != null ? rs.getDouble("Total") : null,
                    rs.getString("FechaPago"),
                    rs.getObject("NumeroRecibo") != null ? rs.getInt("NumeroRecibo") : null
            );

    public Optional<TotalCategoriaResponse<Long>> spDashHospedajeTotales(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.querySingle("spDashHospedajeTotales", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashHospedajeTotalesEstatus(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("Desarrollo", desarrollo);
        return spExecutor.queryList("spDashHospedajeTotalesEstatus", params, DashboardRowMappers.crearMapper());
    }

    public List<TotalCategoriaResponse<Long>> spDashHospedajesDesarrollos(String fechaInicio, String fechaFin) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        return spExecutor.queryList("spDashHospedajesDesarrollos", params, DashboardRowMappers.crearMapper());
    }

    public List<HospedajeLibreResponse> spDashHospedajesLibres(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashHospedajesLibres", params, hospedajeLibreMapper);
    }

    public List<HospedajesDisponiblesResponse> spDashDisponibilidadxMes(String mes, String anio, String desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("Mes", mes);
        params.put("Anio", anio);
        params.put("Desarrollo", desarrollo);
        return spExecutor.queryList("spDashDisponibilidadxMes", params, disponibildadMapper);
    }

    public List<HospedajeDetalleMovimientoResponse> spDashHospedajesDetallesMovimientos(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashHospedajesDetallesMovimientos", params, detalleMovimientoMapper);
    }

    public List<ReservasCanceladasResponse> spDashReservasCanceladas(String idDesarrollo, String fechaEntrada, String fechaSalida) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdDesarrollo", idDesarrollo);
        params.put("FechaEntrada", fechaEntrada);
        params.put("FechaSalida", fechaSalida);
        return spExecutor.queryList("spDashReservasCanceladas", params, reservasCanceladasMapper);
    }

    public List<SinReservaDetalleMovimientoResponse> spDashSinReservaDetallesMovimientos(String fechaInicio, String fechaFin, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollo", desarrollo);
        return spExecutor.queryList("spDashSinReservaDetallesMovimientos", params, sinReservaMapper);
    }
}
