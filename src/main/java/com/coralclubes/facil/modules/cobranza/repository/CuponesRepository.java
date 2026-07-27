package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponDetalleResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponListadoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
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

    private final RowMapper<CuponListadoResponse> cuponListadoMapper = (rs, rowNum) -> new CuponListadoResponse(
            rs.getObject("id", Integer.class),
            rs.getString("nombre"),
            rs.getObject("anio", Integer.class),
            rs.getString("descripcion"),
            rs.getString("origen"),
            rs.getString("destino"),
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

    public List<SelectGenerico<Integer>> spCuponesCatalogoOrigenes() {
        return spExecutor.queryList("spCuponesCatalogoOrigenes", Collections.emptyMap(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spCuponesCatalogoDestinos() {
        return spExecutor.queryList("spCuponesCatalogoDestinos", Collections.emptyMap(), selectGenericoMapper);
    }

    public Optional<Integer> spCuponesGuardarCupon(GuardarCuponRequest request, String usuario) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", request.id());
            params.put("nombre", request.nombre());
            params.put("year", request.year());
            params.put("descripcion", request.descripcion());
            params.put("origen", request.origen());
            params.put("destino", request.destino());
            params.put("inicio_vigencia", request.inicioVigencia());
            params.put("fin_vigencia", request.finVigencia());
            params.put("es_transferible", request.esTransferible());
            params.put("nomenclatura", request.nomenclatura());
            params.put("desarrollo_id", request.desarrollo());
            params.put("usuario", usuario);

            // Serializamos explícitamente a String para SQL Server
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
            Integer origen,
            Integer destino
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("desarrollo", desarrollo);
        params.put("origen", origen);
        params.put("destino", destino);

        return spExecutor.queryList("spCuponesObtenerListadoCupones", params, cuponListadoMapper);
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
                        rs.getInt("origen"),
                        rs.getInt("destino"),
                        rs.getTimestamp("inicioVigencia").toLocalDateTime(),
                        rs.getTimestamp("finVigencia").toLocalDateTime(),
                        rs.getBoolean("esTransferible"),
                        rs.getString("nomenclatura"),
                        rs.getInt("desarrollo"),
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
        
        return spExecutor.queryList("spCuponesObtenerCuponesDesactivados", params, cuponListadoMapper);
    }
}
