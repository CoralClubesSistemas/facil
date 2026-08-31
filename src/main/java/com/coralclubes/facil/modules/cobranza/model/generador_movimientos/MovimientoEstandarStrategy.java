package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoManualResponse;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MovimientoEstandarStrategy implements GeneracionMovimientoStrategy {

    private final GeneracionMovimientosRepository repository;
    private final BusinessLogger logger;

    private static final List<Integer> IDS_ESPECIALES = List.of(
            MovimientoCredencialesStrategy.TIPO_MOVIMIENTO_CREDENCIALES // 25
    );

    @Override
    public boolean soporta(Integer tipoMovimientoId) {
        return tipoMovimientoId != null && !IDS_ESPECIALES.contains(tipoMovimientoId);
    }

    @Override
    public List<MovimientoManualResponse> generar(GeneracionMovimientoRequest request, String usuario) {
        Map<String, Object> params = request.getParametrosEspeciales() != null 
                ? request.getParametrosEspeciales() 
                : Map.of();

        Integer cantidad = 1;
        if (params.containsKey("cantidadMovimientos") && params.get("cantidadMovimientos") != null) {
            Object cantVal = params.get("cantidadMovimientos");
            if (cantVal instanceof Number num) {
                cantidad = num.intValue();
            } else {
                cantidad = Integer.parseInt(cantVal.toString());
            }
        }

        String descripcion = "";
        if (params.containsKey("descripcion") && params.get("descripcion") != null) {
            descripcion = params.get("descripcion").toString();
        }

        java.math.BigDecimal cuota = java.math.BigDecimal.ZERO;
        if (params.containsKey("cuota") && params.get("cuota") != null) {
            Object cuotaVal = params.get("cuota");
            if (cuotaVal instanceof java.math.BigDecimal bigDecimal) {
                cuota = bigDecimal;
            } else if (cuotaVal instanceof Number num) {
                cuota = java.math.BigDecimal.valueOf(num.doubleValue());
            } else {
                cuota = new java.math.BigDecimal(cuotaVal.toString());
            }
        }

        logger.info(usuario, "Generando movimiento estándar: {}", Map.of(
                "membresia", request.getMembresia(),
                "tipoMovimientoId", request.getTipoMovimientoId(),
                "cantidad", cantidad,
                "descripcion", descripcion,
                "cuota", cuota,
                "fechaVencimiento", request.getFechaVencimiento() != null ? request.getFechaVencimiento() : LocalDate.now(),
                "desarrolloConsumo", request.getDesarrolloConsumo() != null ? request.getDesarrolloConsumo() : 0
        ));

        LocalDate fechaVencimiento = request.getFechaVencimiento() != null 
                ? request.getFechaVencimiento() 
                : LocalDate.now();

        return repository.spCobranzaInsertaMovimientoManual(
                request.getMembresia(),
                request.getTipoMovimientoId(),
                cantidad,
                descripcion,
                cuota,
                fechaVencimiento,
                request.getDesarrolloConsumo(),
                usuario
        );
    }
}
