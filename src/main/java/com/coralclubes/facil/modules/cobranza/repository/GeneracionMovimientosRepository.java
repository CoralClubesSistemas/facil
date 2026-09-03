package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionCredencialesResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MapeoPeriodicidadResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoManualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.TarifaMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.UltimoMovimientoResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GeneracionMovimientosRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<TarifaMovimientoResponse> tarifaMovimientoMapper = (rs, rowNum) ->
            TarifaMovimientoResponse.builder()
                    .anio(rs.getObject("anio") != null ? rs.getInt("anio") : null)
                    .cuota(rs.getBigDecimal("cuota"))
                    .build();

    private final RowMapper<MapeoPeriodicidadResponse> mapeoPeriodicidadMapper = (rs, rowNum) ->
            MapeoPeriodicidadResponse.builder()
                    .periodicidadId(rs.getObject("periodicidad_id") != null ? rs.getInt("periodicidad_id") : null)
                    .periodoUnidad(rs.getString("periodo_unidad"))
                    .cantidadXPeriodo(rs.getObject("cantidad_x_periodo") != null ? rs.getInt("cantidad_x_periodo") : null)
                    .build();

    private final RowMapper<UltimoMovimientoResponse> ultimoMovimientoMapper = (rs, rowNum) ->
            UltimoMovimientoResponse.builder()
                    .membresia(rs.getString("membresia"))
                    .idMovimiento(rs.getObject("idMovimiento") != null ? rs.getInt("idMovimiento") : null)
                    .numeroPlan(rs.getObject("numeroPlan") != null ? rs.getInt("numeroPlan") : null)
                    .fechaGeneracion(rs.getTimestamp("fechaGeneracion") != null
                            ? rs.getTimestamp("fechaGeneracion").toLocalDateTime()
                            : null)
                    .fechaVencimiento(rs.getTimestamp("fechaVencimiento") != null
                            ? rs.getTimestamp("fechaVencimiento").toLocalDateTime()
                            : null)
                    .importeCargo(rs.getBigDecimal("importeCargo"))
                    .importeAbono(rs.getBigDecimal("importeAbono"))
                    .importePendiente(rs.getBigDecimal("importePendiente"))
                    .usuarioGenera(rs.getString("usuarioGenera"))
                    .estatus(rs.getObject("estatus") != null ? rs.getInt("estatus") : null)
                    .estatusMovimiento(rs.getString("estatusMovimiento"))
                    .numeroBeneficiarios(rs.getObject("numeroBeneficiarios") != null ? rs.getInt("numeroBeneficiarios") : null)
                    .concepto(rs.getString("concepto"))
                    .descripcion(rs.getString("descripcion"))
                    .idTipoMovimiento(rs.getObject("idTipoMovimiento") != null ? rs.getInt("idTipoMovimiento") : null)
                    .periodicidadId(rs.getObject("periodicidadId") != null ? rs.getInt("periodicidadId") : null)
                    .periodicidad(rs.getString("periodicidad"))
                    .baseDeCobroId(rs.getObject("baseDeCobroId") != null ? rs.getInt("baseDeCobroId") : null)
                    .baseDeCobro(rs.getString("baseDeCobro"))
                    .tipoMovimiento(rs.getString("tipoMovimiento"))
                    .build();

    private final RowMapper<MovimientoManualResponse> movimientoManualMapper = (rs, rowNum) ->
            MovimientoManualResponse.builder()
                    .membresia(rs.getString("membresia"))
                    .mvtId(rs.getObject("mvt_id") != null ? rs.getInt("mvt_id") : null)
                    .descripcion(rs.getString("descripcion"))
                    .fechaVencimiento(rs.getTimestamp("fecha_vencimiento") != null
                            ? rs.getTimestamp("fecha_vencimiento").toLocalDateTime()
                            : null)
                    .cuota(rs.getBigDecimal("cuota"))
                    .build();

    private final RowMapper<MovimientoPorTipoMembresiaResponse> movimientoMapper = (rs, rowNum) -> {

        boolean cuotaForzosa = rs.getObject("cuotaForzosa") != null
                && rs.getBoolean("cuotaForzosa");

        boolean esValido = !cuotaForzosa
                || rs.getBigDecimal("cuota") != null;

        Integer baseDeCobroId = rs.getObject("baseDeCobroId") != null
                ? rs.getInt("baseDeCobroId")
                : null;

        return MovimientoPorTipoMembresiaResponse.builder()
                .id(rs.getInt("id"))
                .descripcion(rs.getString("descripcion"))
                .periodicidadId(rs.getObject("periodicidad_id") != null ? rs.getInt("periodicidad_id") : null)
                .periodicidad(rs.getString("periodicidad"))
                .baseDeCobroId(baseDeCobroId)
                .baseDeCobro(rs.getString("baseDeCobro"))
                .generaInteres(
                        rs.getObject("generaInteres") != null
                                ? rs.getBoolean("generaInteres")
                                : null
                )
                .cuota(rs.getBigDecimal("cuota"))
                .anioVigencia(
                        rs.getObject("anioVigencia") != null
                                ? rs.getInt("anioVigencia")
                                : null
                )
                .cuotaForzosa(
                        rs.getObject("cuotaForzosa") != null
                                ? rs.getBoolean("cuotaForzosa")
                                : null
                )
                .esValido(esValido)
                .build();
    };

    private final RowMapper<CotizacionCredencialesResponse> cotizacionCredencialesMapper = (rs, rowNum) ->
            CotizacionCredencialesResponse.builder()
                    .tarifaEstablecida(rs.getBigDecimal("tarifaEstablecida"))
                    .cantidadBeneficiarios(rs.getObject("cantidadBeneficiarios") != null ? rs.getInt("cantidadBeneficiarios") : null)
                    .cantidadMovimientosAInsertar(rs.getObject("cantidadMovimientosAInsertar") != null ? rs.getInt("cantidadMovimientosAInsertar") : null)
                    .cantidadMovimientosAModificar(rs.getObject("cantidadMovimientosAModificar") != null ? rs.getInt("cantidadMovimientosAModificar") : null)
                    .calculoTotal(rs.getBigDecimal("calculoTotal"))
                    .cuotaPorRegitro(rs.getBigDecimal("cuotaPorRegitro"))
                    .build();

    public List<MovimientoPorTipoMembresiaResponse> spCobranzaObtenerMovimientosPorTipoMembresia(String membresia) {
        Map<String, Object> params = Map.of(
                "membresia", membresia
        );

        return spExecutor.queryList(
                "spCobranzaObtenerMovimientosPorTipoMembresia",
                params,
                movimientoMapper
        );
    }

    public List<MovimientoManualResponse> spCobranzaInsertaMovimientoManual(
            String membresia,
            Integer tipoMovimiento,
            Integer cantidad,
            String descripcion,
            BigDecimal cuota,
            LocalDateTime fechaVencimiento,
            Integer desarrolloConsumo,
            String usuario
    ) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "tipoMovimiento", tipoMovimiento,
                "cantidad", cantidad,
                "descripcion", descripcion != null ? descripcion : "",
                "cuota", cuota,
                "fechaVencimiento", fechaVencimiento,
                "desarrolloConsumo", desarrolloConsumo,
                "usuario", usuario
        );

        return spExecutor.queryList(
                "spCobranzaInsertaMovimientoManual",
                params,
                movimientoManualMapper
        );
    }

    public List<MovimientoManualResponse> spCobranzaInsertaMovimientoCredenciales(
            String membresia,
            Integer anios,
            Boolean incluirPrevios,
            LocalDateTime fechaVencimiento,
            Integer desarrolloConsumo,
            String usuario
    ) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "anios", anios,
                "incluirPrevios", incluirPrevios != null ? incluirPrevios : false,
                "fechaVencimiento", fechaVencimiento,
                "desarrolloConsumo", desarrolloConsumo,
                "usuario", usuario
        );

        return spExecutor.queryList(
                "spCobranzaInsertaMovimientoCredenciales",
                params,
                movimientoManualMapper
        );
    }

    public CotizacionCredencialesResponse spCobranzaConsultarCotizacionCredenciales(
            String membresia,
            Integer anios,
            Boolean incluirPrevios,
            Integer desarrolloConsumo
    ) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "anios", anios,
                "incluirPrevios", incluirPrevios != null ? incluirPrevios : false,
                "desarrolloConsumo", desarrolloConsumo
        );

        return spExecutor.querySingle(
                "spCobranzaConsultarCotizacionCredenciales",
                params,
                cotizacionCredencialesMapper
        ).orElse(null);
    }

    public Optional<UltimoMovimientoResponse> spCobranzaObtenerUltimoMovimiento(
            String membresia,
            Integer desarrolloConsumo,
            Integer tipoMovimiento,
            Integer estatus
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("membresia", membresia);
        params.put("desarrolloConsumo", desarrolloConsumo);
        params.put("tipoMovimiento", tipoMovimiento);
        params.put("estatus", estatus);

        return spExecutor.querySingle(
                "spCobranzaObtenerUltimoMovimiento",
                params,
                ultimoMovimientoMapper
        );
    }

    public List<MapeoPeriodicidadResponse> spCobranzaMapeoPeriodicidad(Integer periodicidad, String periodo) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodicidad", periodicidad);
        params.put("periodo", periodo);

        return spExecutor.queryList(
                "spCobranzaMapeoPeriodicidad",
                params,
                mapeoPeriodicidadMapper
        );
    }

    public Optional<TarifaMovimientoResponse> spCobranzaObtenerTarifaMovimiento(
            String membresia,
            Integer tipoMovimiento,
            Integer anio
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("membresia", membresia);
        params.put("tipoMovimiento", tipoMovimiento);
        params.put("anio", anio);

        return spExecutor.querySingle(
                "spCobranzaObtenerTarifaMovimiento",
                params,
                tarifaMovimientoMapper
        );
    }
}
