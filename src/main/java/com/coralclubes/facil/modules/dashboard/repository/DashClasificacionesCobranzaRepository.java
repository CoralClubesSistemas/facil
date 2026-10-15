package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DashClasificacionesCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<ClasificacionesResponse> clasificacionesMapper = (rs, rowNum) ->
            new ClasificacionesResponse(
                    rs.getString("movimiento"),
                    rs.getString("membresias"),
                    rs.getBigDecimal("cargo"),
                    rs.getBigDecimal("pendiente"),
                    rs.getBigDecimal("descuento"),
                    rs.getBigDecimal("bonificado"),
                    rs.getBigDecimal("intereses"),
                    rs.getBigDecimal("interesesBonificados"),
                    rs.getBigDecimal("interesesPagados"),
                    rs.getBigDecimal("neto")
            );

    private final RowMapper<ClasificacionesDetallesResponse> clasificacionesDetallesMapper = (rs, rowNum) ->
            new ClasificacionesDetallesResponse(
                    rs.getObject("id") != null ? rs.getInt("id") : null,
                    rs.getString("desarrollo"),
                    rs.getString("membresia"),
                    rs.getString("movimiento"),
                    rs.getString("fechaPago"),
                    rs.getBigDecimal("cargo"),
                    rs.getBigDecimal("pendiente"),
                    rs.getBigDecimal("descuento"),
                    rs.getBigDecimal("bonificado"),
                    rs.getBigDecimal("intereses"),
                    rs.getBigDecimal("interesesBonificados"),
                    rs.getBigDecimal("interesesPagados"),
                    rs.getBigDecimal("neto"),
                    rs.getString("recibo")
            );

    public List<ClasificacionesResponse> spDashTotalesClasificaciones(String fechaInicio, String fechaFin, String desarrollos, String clasificaciones) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollos", desarrollos);
        params.put("clasificaciones", clasificaciones);
        return spExecutor.queryList("spDashTotalesClasificaciones", params, clasificacionesMapper);
    }

    public List<ClasificacionesDetallesResponse> spDashClasificacionesDetalles(String fechaInicio, String fechaFin, String desarrollos, String clasificaciones) {
        Map<String, Object> params = new HashMap<>();
        params.put("fechaInicio", fechaInicio);
        params.put("fechaFin", fechaFin);
        params.put("desarrollos", desarrollos);
        params.put("clasificaciones", clasificaciones);
        return spExecutor.queryList("spDashClasificacionesDetalles", params, clasificacionesDetallesMapper);
    }
}
