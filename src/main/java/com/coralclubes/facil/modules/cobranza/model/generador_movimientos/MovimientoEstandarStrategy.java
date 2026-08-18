package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
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
    public void generar(GeneracionMovimientoRequest request, String usuario) {
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

        logger.info(usuario, "Generando movimiento estándar: {}", Map.of(
                "membresia", request.getMembresia(),
                "tipoMovimientoId", request.getTipoMovimientoId(),
                "cantidad", cantidad,
                "descripcion", request.getDescripcion(),
                "cuota", request.getCuota(),
                "fechaVencimiento", request.getFechaVencimiento(),
                "desarrolloConsumo", request.getDesarrolloConsumo()
        ));

        LocalDate fechaVencimiento = request.getFechaVencimiento() != null 
                ? request.getFechaVencimiento() 
                : LocalDate.now();

        repository.spCobranzaInsertaMovimientoManual(
                request.getMembresia(),
                request.getTipoMovimientoId(),
                cantidad,
                request.getDescripcion(),
                request.getCuota(),
                fechaVencimiento,
                request.getDesarrolloConsumo(),
                usuario
        );
    }
}
