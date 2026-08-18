package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionCredencialesResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GeneracionMovimientosRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<MovimientoPorTipoMembresiaResponse> movimientoMapper = (rs, rowNum) ->
            MovimientoPorTipoMembresiaResponse.builder()
                    .id(rs.getInt("id"))
                    .descripcion(rs.getString("descripcion"))
                    .periodicidad(rs.getString("periodicidad"))
                    .baseDeCobro(rs.getString("baseDeCobro"))
                    .generaInteres(rs.getObject("generaInteres") != null ? rs.getBoolean("generaInteres") : null)
                    .cuota(rs.getBigDecimal("cuota"))
                    .anioVigencia(rs.getObject("anioVigencia") != null ? rs.getInt("anioVigencia") : null)
                    .build();

    public List<MovimientoPorTipoMembresiaResponse> spCobranzaObtenerMovimientosPorTipoMembresia(Integer tipoMembresia) {
        Map<String, Object> params = Map.of(
                "tipoMembresia", tipoMembresia
        );

        return spExecutor.queryList(
                "spCobranzaObtenerMovimientosPorTipoMembresia",
                params,
                movimientoMapper
        );
    }

    public void spCobranzaInsertaMovimientoManual(
            String membresia,
            Integer tipoMovimiento,
            Integer cantidad,
            String descripcion,
            BigDecimal cuota,
            LocalDate fechaVencimiento,
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

        spExecutor.execute("spCobranzaInsertaMovimientoManual", params);
    }

    public void spCobranzaInsertaMovimientoCredenciales(
            String membresia,
            Integer anios,
            Boolean incluirPrevios,
            LocalDate fechaVencimiento,
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

        spExecutor.execute("spCobranzaInsertaMovimientoCredenciales", params);
    }

    private final RowMapper<CotizacionCredencialesResponse> cotizacionCredencialesMapper = (rs, rowNum) ->
            CotizacionCredencialesResponse.builder()
                    .tarifaEstablecida(rs.getBigDecimal("tarifaEstablecida"))
                    .cantidadBeneficiarios(rs.getObject("cantidadBeneficiarios") != null ? rs.getInt("cantidadBeneficiarios") : null)
                    .cantidadMovimientosAInsertar(rs.getObject("cantidadMovimientosAInsertar") != null ? rs.getInt("cantidadMovimientosAInsertar") : null)
                    .cantidadMovimientosAModificar(rs.getObject("cantidadMovimientosAModificar") != null ? rs.getInt("cantidadMovimientosAModificar") : null)
                    .calculoTotal(rs.getBigDecimal("calculoTotal"))
                    .cuotaPorRegitro(rs.getBigDecimal("cuotaPorRegitro"))
                    .build();

    public CotizacionCredencialesResponse spCobranzaConsultarCotizacionCredenciales(
            String membresia,
            Integer anios,
            Boolean incluirPrevios,
            Integer desarrolloConsumo
    ) {
        System.out.println("spCobranzaConsultarCotizacionCredenciales called with: membresia=" + membresia + ", anios=" + anios + ", incluirPrevios=" + incluirPrevios + ", desarrolloConsumo=" + desarrolloConsumo);

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
}
