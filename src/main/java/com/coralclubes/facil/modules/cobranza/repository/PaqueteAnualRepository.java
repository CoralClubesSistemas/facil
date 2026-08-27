package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPaqueteAnualResponse;
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
public class PaqueteAnualRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<MovimientoPaqueteAnualResponse> movimientoPaqueteAnualMapper = (rs, rowNum) ->
            MovimientoPaqueteAnualResponse.builder()
                    .id(rs.getObject("id") != null ? rs.getInt("id") : null)
                    .descripcion(rs.getString("descripcion"))
                    .periodicidad(rs.getString("periodicidad"))
                    .baseDeCobroId(rs.getInt("baseDeCobroId"))
                    .baseDeCobro(rs.getString("baseDeCobro"))
                    .cuota(rs.getBigDecimal("cuota"))
                    .anioVigencia(rs.getObject("anioVigencia") != null ? rs.getInt("anioVigencia") : null)
                    .build();

    private final RowMapper<Integer> paqueteIdMapper = (rs, rowNum) -> rs.getInt("paquete_anual_id");

    public List<MovimientoPaqueteAnualResponse> spCobranzaCatalogoMovimientosPaqueteAnual(Integer anio, Integer tipoMembresia) {
        Map<String, Object> params = Map.of(
                "year", anio,
                "tipo_membresia", tipoMembresia
        );

        return spExecutor.queryList(
                "spCobranzaCatalogoMovimientosPaqueteAnual",
                params,
                movimientoPaqueteAnualMapper
        );
    }

    public Optional<Integer> spCobranzaGuardarPaqueteAnual(
            Integer id,
            Integer anio,
            Integer tipoMembresia,
            Integer clasificacionMembresia,
            Integer desarrollo,
            String usuario,
            String configuracionDescuentosJson,
            String configuracionMovimientosJson
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("year", anio);
        params.put("tipo_membresia", tipoMembresia);
        params.put("clasificacion_membresia", clasificacionMembresia);
        params.put("desarrollo", desarrollo);
        params.put("usuario", usuario);
        params.put("configuracion_descuentos", configuracionDescuentosJson);
        params.put("configuracion_movimientos", configuracionMovimientosJson);

        return spExecutor.querySingle(
                "spCobranzaGuardarPaqueteAnual",
                params,
                paqueteIdMapper
        );
    }
}
