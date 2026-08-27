package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPaqueteAnualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.PaqueteAnualResponse;
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
                    .baseDeCobro(rs.getString("baseDeCobro"))
                    .cuota(rs.getBigDecimal("cuota"))
                    .anioVigencia(rs.getObject("anioVigencia") != null ? rs.getInt("anioVigencia") : null)
                    .build();

    private final RowMapper<PaqueteAnualResponse> paqueteAnualMapper = (rs, rowNum) ->
            PaqueteAnualResponse.builder()
                    .id(rs.getObject("id") != null ? rs.getInt("id") : null)
                    .desarrolloId(rs.getObject("desarrollo_id") != null ? rs.getInt("desarrollo_id") : null)
                    .desarrollo(rs.getString("desarrollo"))
                    .anio(rs.getObject("YEAR") != null ? rs.getInt("YEAR") : null)
                    .tipoMembresiaId(rs.getObject("tipo_membresia_id") != null ? rs.getInt("tipo_membresia_id") : null)
                    .tipoMembresia(rs.getString("tipo_membresia"))
                    .clasificacionMembresiaId(rs.getObject("clasificacion_membresia_id") != null ? rs.getInt("clasificacion_membresia_id") : null)
                    .clasificacionMembresia(rs.getString("clasificacion_membresia"))
                    .fechaRegistro(rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null)
                    .usuarioRegistro(rs.getString("usuario_registro"))
                    .build();

    private final RowMapper<Integer> paqueteIdMapper = (rs, rowNum) -> rs.getInt("paquete_anual_id");
    private final RowMapper<String> jsonStringMapper = (rs, rowNum) -> rs.getString(1);

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

    public List<PaqueteAnualResponse> spCobranzaObtenerPaquetesAnuales(
            Integer anio,
            Integer tipoMembresia,
            Integer clasificacionMembresia,
            Integer desarrollo
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("year", anio);
        params.put("tipo_membresia", tipoMembresia);
        params.put("clasificacion_membresia", clasificacionMembresia);
        params.put("desarrollo", desarrollo);

        return spExecutor.queryList(
                "spCobranzaObtenerPaquetesAnuales",
                params,
                paqueteAnualMapper
        );
    }

    public Optional<String> spCobranzaObtenerPaqueteAnualDetalle(Integer paqueteAnualId) {
        Map<String, Object> params = Map.of("paquete_anual_id", paqueteAnualId);
        List<String> list = spExecutor.queryList("spCobranzaObtenerPaqueteAnualDetalle", params, jsonStringMapper);
        if (list.isEmpty() || list.getFirst() == null || list.getFirst().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(String.join("", list));
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
