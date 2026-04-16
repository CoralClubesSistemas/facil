package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CobranzaCatalogosRepository {
    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<SelectGenerico<Integer>> selectGenericoMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getInt(1), rs.getString(2));

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
}
