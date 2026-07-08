package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CobranzaCatalogosRepository {
    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<SelectGenerico<Integer>> selectGenericoMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getInt(1), rs.getString(2));

    private final RowMapper<SelectGenerico<String>> selectGenericoStringMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getString(1), rs.getString(2));

    private final RowMapper<BigDecimal> porcentaje = (rs, rowNum) ->
            new BigDecimal(rs.getString(1));

    public List<SelectGenerico<Integer>> spCobranzaCatalogoTiposSeries() {
        return spExecutor.queryList("spCobranzaCatalogoTiposSeries", Map.of(), selectGenericoMapper);
    }

    public BigDecimal spCobranzaObtenerPorcentajeLimite (Integer idDesarrollo, Integer clasificacionMembresia) {
        return spExecutor.querySingle("spCobranzaObtenerPorcentajeLimite",
                        Map.of("DesarrolloId", idDesarrollo,
                                "ClasificacionMembresia", clasificacionMembresia),
                        porcentaje)
                .orElse(BigDecimal.ZERO);
    }

    public List<SelectGenerico<Integer>> spCobranzaCatalogoTerminales() {
        return spExecutor.queryList("spCobranzaCatalogoTerminales", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spCobranzaCatalogoBancos() {
        return spExecutor.queryList("spCobranzaCatalogoBancos", Collections.emptyMap(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spCobranzaCatalogoTiposMovimientos() {
        return spExecutor.queryList("spCobranzaCatalogoTiposMovimientos", Collections.emptyMap(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spCobranzaCatalogoEstatusMovimientos() {
        return spExecutor.queryList("spCobranzaCatalogoEstatusMovimientos", Collections.emptyMap(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spCobranzaCatalogoDesarrollos() {
        return spExecutor.queryList("spCobranzaCatalogoDesarrollos", Collections.emptyMap(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spRepoCatalogoTiposMembresias(Integer clasificacion) {
        Map<String, Object> params = new HashMap<>();
        params.put("Clasificacion", clasificacion);

        return spExecutor.queryList("spRepoCatalogoTiposMembresias", params, selectGenericoMapper);
    }
}
