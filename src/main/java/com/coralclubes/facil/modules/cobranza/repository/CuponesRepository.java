package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponDetalleResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponListadoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCanjesPorConceptoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesEstadisticasKpiResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesTopCanjeadosResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesUsoMensualResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CuponesRepository {

    private final StoredProcedureExecutor spExecutor;
    private final ObjectMapper objectMapper;

    private final RowMapper<CuponesCatalogoElementoResponse> catalogoElementoMapper = (rs, rowNum) -> new CuponesCatalogoElementoResponse(rs.getString("clave"), rs.getString("nombre"), rs.getString("descripcion"), rs.getString("tipo"));

    private final RowMapper<SelectGenerico<Integer>> selectGenericoMapper = (rs, rowNum) -> new SelectGenerico<>(rs.getInt("value"), rs.getString("label"));
    private final RowMapper<SelectGenerico<String>> selectStringGenericoMapper = (rs, rowNum) -> new SelectGenerico<>(rs.getString("value"), rs.getString("label"));

    private final RowMapper<CuponListadoResponse> listadoResponseRowMapper = (rs, rowNum) -> new CuponListadoResponse(
            rs.getObject("id", Integer.class),
            rs.getString("nombre"),
            rs.getObject("anio", Integer.class),
            rs.getString("descripcion"),
            rs.getString("origen"),
            rs.getTimestamp("inicio_vigencia") != null ? rs.getTimestamp("inicio_vigencia").toLocalDateTime() : null,
            rs.getTimestamp("fin_vigencia") != null ? rs.getTimestamp("fin_vigencia").toLocalDateTime() : null,
            rs.getObject("es_transferible", Boolean.class),
            rs.getString("nomenclatura"),
            rs.getObject("id_desarrollo", Integer.class),
            rs.getString("desarrollo"),
            rs.getString("imagen")
    );

    public List<CuponesCatalogoElementoResponse> spCuponesCatalogoCondiciones() {
        return spExecutor.queryList("spCuponesCatalogoCondiciones", Collections.emptyMap(), catalogoElementoMapper);
    }

    public List<CuponesCatalogoElementoResponse> spCuponesCatalogoBeneficios() {
        return spExecutor.queryList("spCuponesCatalogoBeneficios", Collections.emptyMap(), catalogoElementoMapper);
    }

    public List<SelectGenerico<String>> spCuponesCatalogoOrigenes() {
        return spExecutor.queryList("spCuponesCatalogoOrigenes", Collections.emptyMap(), selectStringGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spCuponesCatalogoConceptos() {
        return spExecutor.queryList("spCuponesCatalogoConceptos", Collections.emptyMap(), selectGenericoMapper);
    }

    public Optional<Integer> spCuponesGuardarCupon(GuardarCuponRequest request, String usuario) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", request.id());
            params.put("nombre", request.nombre());
            params.put("year", request.year());
            params.put("descripcion", request.descripcion());
            params.put("origen", request.origen());
            params.put("inicio_vigencia", request.inicioVigencia());
            params.put("fin_vigencia", request.finVigencia());
            params.put("es_transferible", request.esTransferible());
            params.put("nomenclatura", request.nomenclatura());
            params.put("desarrollo_id", request.desarrollo());
            params.put("usuario", usuario);

            // Serializamos explícitamente a String JSON para SQL Server
            params.put("desarrollos", objectMapper.writeValueAsString(request.desarrollos()));
            params.put("configuracion_membresias", objectMapper.writeValueAsString(request.configuracionMembresias()));
            params.put("condiciones", objectMapper.writeValueAsString(request.condiciones()));
            params.put("beneficios", objectMapper.writeValueAsString(request.beneficios()));

            return spExecutor.querySingle("spCuponesGuardarCuponInfoGeneral", params, (rs, rowNum) -> rs.getInt(1));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar el JSON para el SP", e);
        }
    }

    public List<CuponListadoResponse> spCuponesObtenerListadoCupones(
            Integer year,
            Integer desarrollo,
            String origen
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("desarrollo", desarrollo);
        params.put("origen", origen);

        return spExecutor.queryList("spCuponesObtenerListadoCupones", params, listadoResponseRowMapper);
    }

    public void spCuponesGuardarImagenCupon(Integer idCupon, String imagen) {
        spExecutor.execute("spCuponesGuardarImagenCupon", Map.of("id", idCupon, "imagen", imagen));
    }

    public void spCuponesModificarEstatusCupon(Integer idCupon, Boolean estatus) {
        spExecutor.execute("spCuponesModificarEstatusCupon", Map.of("id", idCupon, "estatus", estatus));
    }

    public Optional<CuponDetalleResponse> spCuponesObtenerDetalle(Integer id) {
        Map<String, Object> params = Map.of("id", id);

        return spExecutor.querySingle("spCuponesObtenerDetalle", params, (rs, rowNum) -> {
            try {
                // Mapeo JSON de SQL a Clases Java usando ObjectMapper
                List<Integer> desarrollos = null;
                String desarrollosJson = rs.getString("desarrollos");

                if (desarrollosJson != null && !desarrollosJson.isBlank()) {
                    desarrollos = objectMapper.readValue(desarrollosJson, new TypeReference<List<Integer>>() {
                    });
                }

                List<CuponDetalleResponse.PeriodoDto> periodos = objectMapper.readValue(rs.getString("json_periodos"), new TypeReference<List<CuponDetalleResponse.PeriodoDto>>() {
                });
                List<CuponDetalleResponse.MembresiaCantidadesDto> membresias = objectMapper.readValue(rs.getString("json_membresias"), new TypeReference<List<CuponDetalleResponse.MembresiaCantidadesDto>>() {
                });
                List<CuponDetalleResponse.AtributoCuponDto> condiciones = objectMapper.readValue(rs.getString("json_condiciones"), new TypeReference<List<CuponDetalleResponse.AtributoCuponDto>>() {
                });
                List<CuponDetalleResponse.AtributoCuponDto> beneficios = objectMapper.readValue(rs.getString("json_beneficios"), new TypeReference<List<CuponDetalleResponse.AtributoCuponDto>>() {
                });

                return new CuponDetalleResponse(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("year"),
                        rs.getString("descripcion"),
                        rs.getString("origen"),
                        rs.getTimestamp("inicioVigencia").toLocalDateTime(),
                        rs.getTimestamp("finVigencia").toLocalDateTime(),
                        rs.getBoolean("esTransferible"),
                        rs.getString("nomenclatura"),
                        rs.getInt("desarrollo"),
                        desarrollos,
                        new CuponDetalleResponse.ConfiguracionMembresiasDto(periodos, membresias),
                        condiciones,
                        beneficios
                );
            } catch (Exception e) {
                throw new RuntimeException("Error al mapear detalle del cupón", e);
            }
        });
    }

    public List<CuponListadoResponse> spCuponesObtenerCuponesDesactivados(Integer year, Integer desarrollo) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("desarrollo", desarrollo);

        return spExecutor.queryList("spCuponesObtenerCuponesDesactivados", params, listadoResponseRowMapper);
    }

    private final RowMapper<CuponesEstadisticasKpiResponse> kpiMapper = (rs, rowNum) -> new CuponesEstadisticasKpiResponse(
            rs.getObject("total_emitidos", Integer.class),
            rs.getObject("total_canjeados", Integer.class),
            rs.getObject("total_disponibles", Integer.class)
    );

    private final RowMapper<CuponesUsoMensualResponse> usoMensualMapper = (rs, rowNum) -> new CuponesUsoMensualResponse(
            rs.getObject("mes", Integer.class),
            rs.getObject("anio", Integer.class),
            rs.getObject("cantidad_canjeada", Integer.class)
    );

    private final RowMapper<CuponesCanjesPorConceptoResponse> canjesPorConceptoMapper = (rs, rowNum) -> new CuponesCanjesPorConceptoResponse(
            rs.getString("concepto"),
            rs.getObject("cantidad_canjeada", Integer.class)
    );

    private final RowMapper<CuponesTopCanjeadosResponse> topCanjeadosMapper = (rs, rowNum) -> new CuponesTopCanjeadosResponse(
            rs.getString("nomenclatura"),
            rs.getString("nombre"),
            rs.getObject("cantidad_canjeada", Integer.class)
    );

    public Optional<CuponesEstadisticasKpiResponse> spCuponesEstadisticasKPIs(Integer anio, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        params.put("desarrollo", desarrollo);

        return spExecutor.querySingle("spCuponesEstadisticasKPIs", params, kpiMapper);
    }

    public List<CuponesUsoMensualResponse> spCuponesEstadisticasUsoMensual(Integer anio, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        params.put("desarrollo", desarrollo);

        return spExecutor.queryList("spCuponesEstadisticasUsoMensual", params, usoMensualMapper);
    }

    public List<CuponesCanjesPorConceptoResponse> spCuponesEstadisticasCanjesPorConcepto(Integer anio, Integer desarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        params.put("desarrollo", desarrollo);

        return spExecutor.queryList("spCuponesEstadisticasCanjesPorConcepto", params, canjesPorConceptoMapper);
    }

    public List<CuponesTopCanjeadosResponse> spCuponesEstadisticasTopCanjeados(Integer anio, Integer desarrollo, Integer top) {
        Map<String, Object> params = new HashMap<>();
        params.put("anio", anio);
        params.put("desarrollo", desarrollo);
        params.put("top", top != null ? top : 5);

        return spExecutor.queryList("spCuponesEstadisticasTopCanjeados", params, topCanjeadosMapper);
    }

    public void spCuponesDuplicarMasivo(List<Integer> ids, Integer targetYear, String usuario) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("json_ids", objectMapper.writeValueAsString(ids));
            params.put("target_year", targetYear);
            params.put("usuario", usuario);

            spExecutor.execute("spCuponesDuplicarMasivo", params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar el listado de IDs para duplicado masivo", e);
        }
    }
}
