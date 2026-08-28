package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioPaqueteAnualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.EsquemaPagoPropuestaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPaqueteAnualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.PaqueteAnualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.PeriodicidadMantenimientoResponse;
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
                    .aplicaPeriodicidad(rs.getObject("aplicaPeriodicidad") != null && rs.getInt("aplicaPeriodicidad") == 1)
                    .baseDeCobroId(rs.getObject("baseDeCobroId") != null ? rs.getInt("baseDeCobroId") : null)
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

    private final RowMapper<EsquemaPagoPropuestaResponse> esquemaPagoPropuestaMapper = (rs, rowNum) ->
            EsquemaPagoPropuestaResponse.builder()
                    .paqueteId(rs.getObject("paquete_id") != null ? rs.getInt("paquete_id") : null)
                    .value(rs.getString("value"))
                    .label(rs.getString("label"))
                    .descuento(rs.getBigDecimal("descuento"))
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

    public List<EsquemaPagoPropuestaResponse> spCobranzaObtenerEsquemasPagoPropuestaPaqueteAnual(String membresia, Integer anio) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "anio", anio
        );

        return spExecutor.queryList(
                "spCobranzaObtenerEsquemasPagoPropuestaPaqueteAnual",
                params,
                esquemaPagoPropuestaMapper
        );
    }

    private final RowMapper<PeriodicidadMantenimientoResponse> periodicidadMapper = (rs, rowNum) ->
            PeriodicidadMantenimientoResponse.builder()
                    .periodicidadId(rs.getObject("periodicidad_id") != null ? rs.getInt("periodicidad_id") : null)
                    .periodicidad(rs.getString("periodicidad"))
                    .cantidadPorPeriodo(rs.getObject("cantidad_por_periodo") != null ? rs.getInt("cantidad_por_periodo") : null)
                    .build();

    public List<PeriodicidadMantenimientoResponse> spCobranzaObtenerPeriodicidadMantenimiento(String membresia) {
        Map<String, Object> params = Map.of("membresia", membresia);
        return spExecutor.queryList("spCobranzaObtenerPeriodicidadMantenimiento", params, periodicidadMapper);
    }

    public Integer spCobranzaObtenerBeneficiariosPaqueteAnual(String membresia) {
        Map<String, Object> params = Map.of("membresia", membresia);
        return spExecutor.querySingle(
                "spCobranzaObtenerBeneficiariosPaqueteAnual",
                params,
                (rs, rowNum) -> rs.getInt("totalBeneficiarios")
        ).orElse(0);
    }

    public Optional<Integer> spCobranzaObtenerPaqueteAnualActivoIdPorMembresia(String membresia, Integer anio) {
        // Obtenemos los esquemas para extraer el paquete_id activo asociado a la membresía
        List<EsquemaPagoPropuestaResponse> esquemas = spCobranzaObtenerEsquemasPagoPropuestaPaqueteAnual(membresia, anio);
        if (esquemas.isEmpty() || esquemas.getFirst().paqueteId() == null) {
            return Optional.empty();
        }
        return Optional.of(esquemas.getFirst().paqueteId());
    }

    private final RowMapper<CuponBeneficioPaqueteAnualResponse> cuponBeneficioMapper = (rs, rowNum) ->
            CuponBeneficioPaqueteAnualResponse.builder()
                    .cuponId(rs.getObject("cupon_id") != null ? rs.getInt("cupon_id") : null)
                    .cupon(rs.getString("cupon"))
                    .nomenclatura(rs.getString("nomenclatura"))
                    .cantidadCupones(rs.getObject("cantidad_cupones") != null ? rs.getInt("cantidad_cupones") : null)
                    .periodoInicio(rs.getTimestamp("periodo_inicio") != null ? rs.getTimestamp("periodo_inicio").toLocalDateTime() : null)
                    .periodoFin(rs.getTimestamp("periodo_fin") != null ? rs.getTimestamp("periodo_fin").toLocalDateTime() : null)
                    .inicioVigenciaPeriodo(rs.getTimestamp("inicio_vigencia_periodo") != null ? rs.getTimestamp("inicio_vigencia_periodo").toLocalDateTime() : null)
                    .finVigenciaPeriodo(rs.getTimestamp("fin_vigencia_periodo") != null ? rs.getTimestamp("fin_vigencia_periodo").toLocalDateTime() : null)
                    .build();

    public List<CuponBeneficioPaqueteAnualResponse> spCobranzaObtenerCuponesBeneficioPaqueteAnual(
            String membresia,
            Integer anio,
            java.time.LocalDateTime fechaCotizacion
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("membresia", membresia);
        params.put("anio", anio);
        params.put("fecha_cotizacion", fechaCotizacion);

        return spExecutor.queryList(
                "spCobranzaObtenerCuponesBeneficioPaqueteAnual",
                params,
                cuponBeneficioMapper
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
