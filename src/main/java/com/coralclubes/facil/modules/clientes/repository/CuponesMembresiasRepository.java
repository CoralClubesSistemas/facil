package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.request.AsignarCuponesMembresiaRequest;
import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleAsignacionResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaDetalleResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaResumenResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CuponesMembresiasRepository {

    private final StoredProcedureExecutor spExecutor;
    private final ObjectMapper objectMapper;

    private final RowMapper<CuponMembresiaResumenResponse> resumenMapper = (rs, rowNum) -> new CuponMembresiaResumenResponse(
            rs.getString("membresia"),
            rs.getObject("id_cupon", Integer.class),
            rs.getObject("movimiento_generador_id", Integer.class),
            rs.getString("movimiento_generador"),
            rs.getObject("cupones_otorgados", Integer.class),
            rs.getObject("cupones_disponibles", Integer.class),
            rs.getString("estatus"),
            rs.getTimestamp("fecha_otorgado") != null ? rs.getTimestamp("fecha_otorgado").toLocalDateTime() : null,
            rs.getString("nombre_cupon"),
            rs.getString("nomenclatura"),
            rs.getString("desarrollo"),
            rs.getString("origen_cupon"),
            rs.getObject("anio_cupon", Integer.class),
            rs.getTimestamp("inicio_vigencia") != null ? rs.getTimestamp("inicio_vigencia").toLocalDateTime() : null,
            rs.getTimestamp("fin_vigencia") != null ? rs.getTimestamp("fin_vigencia").toLocalDateTime() : null
    );

    private final RowMapper<CuponMembresiaDetalleResponse> detalleMapper = (rs, rowNum) -> new CuponMembresiaDetalleResponse(
            rs.getObject("consecutivo", Integer.class),
            rs.getObject("numero_orden", Integer.class),
            rs.getString("folio_descripcion"),
            rs.getString("estatus"),
            rs.getObject("estatus_id", Integer.class),
            rs.getTimestamp("fecha_otorgado") != null ? rs.getTimestamp("fecha_otorgado").toLocalDateTime() : null,
            rs.getTimestamp("fecha_descuento") != null ? rs.getTimestamp("fecha_descuento").toLocalDateTime() : null,
            rs.getString("usuario_otorga"),
            rs.getString("usuario_descuenta"),
            rs.getObject("cupon_id", Integer.class),
            rs.getString("nombre_cupon")
    );

    private final RowMapper<CuponDisponibleAsignacionResponse> disponibleMapper = (rs, rowNum) -> new CuponDisponibleAsignacionResponse(
            rs.getObject("cupon_id", Integer.class),
            rs.getString("nombre_cupon"),
            rs.getString("nomenclatura"),
            rs.getObject("anio", Integer.class),
            rs.getTimestamp("inicio_vigencia") != null ? rs.getTimestamp("inicio_vigencia").toLocalDateTime() : null,
            rs.getTimestamp("fin_vigencia") != null ? rs.getTimestamp("fin_vigencia").toLocalDateTime() : null,
            rs.getObject("es_transferible", Boolean.class),
            rs.getObject("origen_id", Integer.class),
            rs.getString("origen_nombre"),
            rs.getObject("cantidad_cupones", Integer.class),
            rs.getString("nombre_periodo"),
            rs.getTimestamp("fecha_inicio_periodo") != null ? rs.getTimestamp("fecha_inicio_periodo").toLocalDateTime() : null,
            rs.getTimestamp("fecha_fin_periodo") != null ? rs.getTimestamp("fecha_fin_periodo").toLocalDateTime() : null
    );

    public List<CuponMembresiaResumenResponse> spMembresiaObtenerCupones(String membresia, Integer year) {
        Map<String, Object> params = new HashMap<>();
        params.put("membresia", membresia);
        params.put("year", year);

        return spExecutor.queryList("spMembresiaObtenerCupones", params, resumenMapper);
    }

    public List<CuponMembresiaDetalleResponse> spMembresiaObtenerDetalleCupon(Integer cuponId, String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("cupon_id", cuponId);
        params.put("membresia", membresia);

        return spExecutor.queryList("spMembresiaObtenerDetalleCupon", params, detalleMapper);
    }

    public List<CuponDisponibleAsignacionResponse> spCuponesObtenerDisponiblesParaAsignacion(
            String membresia,
            Integer desarrollo,
            Integer tipoMembresiaId,
            Integer anio,
            LocalDateTime fechaAsignacion
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("membresia", membresia);
        params.put("desarrollo", desarrollo);
        params.put("tipo_membresia_id", tipoMembresiaId);
        params.put("anio", anio);
        params.put("fecha_asignacion", fechaAsignacion);

        return spExecutor.queryList("spCuponesObtenerDisponiblesParaAsignacion", params, disponibleMapper);
    }

    public void spCuponesAsignarAMembresia(AsignarCuponesMembresiaRequest request, String usuario) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("membresia", request.membresia());
            params.put("desarrollo", request.desarrollo());
            params.put("usuario", usuario);
            params.put("json_cupones", objectMapper.writeValueAsString(request.cupones()));

            System.out.println("JSON de cupones para asignación: " + params.get("json_cupones")); // Debugging line
            System.out.println("Parámetros para spCuponesAsignarAMembresia: " + params); // Debugging line

            spExecutor.execute("spCuponesAsignarAMembresia", params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar el JSON de cupones para asignación", e);
        }
    }
}
