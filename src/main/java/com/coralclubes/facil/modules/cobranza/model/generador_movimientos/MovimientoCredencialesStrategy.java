package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MovimientoCredencialesStrategy implements GeneracionMovimientoStrategy {

    public static final int TIPO_MOVIMIENTO_CREDENCIALES = 25;

    private final GeneracionMovimientosRepository repository;
    private final BusinessLogger logger;

    @Override
    public boolean soporta(Integer tipoMovimientoId) {
        return tipoMovimientoId != null && tipoMovimientoId == TIPO_MOVIMIENTO_CREDENCIALES;
    }

    @Override
    public void generar(GeneracionMovimientoRequest request, String usuario) {
        Map<String, Object> params = request.getParametrosEspeciales() != null
                ? request.getParametrosEspeciales() 
                : Map.of();

        Integer anios = 1;
        if (params.containsKey("anios") && params.get("anios") != null) {
            Object aniosVal = params.get("anios");
            if (aniosVal instanceof Number num) {
                anios = num.intValue();
            } else {
                anios = Integer.parseInt(aniosVal.toString());
            }
        }

        logger.info(usuario, "Solicitud de generacion de credenciales a {} años para la membresia {}", anios, request.getMembresia());

        Boolean incluirPrevios = false;
        if (params.containsKey("incluyePrevios") && params.get("incluyePrevios") != null) {
            Object previosVal = params.get("incluyePrevios");
            if (previosVal instanceof Boolean bool) {
                incluirPrevios = bool;
            } else {
                incluirPrevios = Boolean.parseBoolean(previosVal.toString());
            }
        }

        LocalDate fechaVencimiento = request.getFechaVencimiento() != null 
                ? request.getFechaVencimiento() 
                : LocalDate.now();

        repository.spCobranzaInsertaMovimientoCredenciales(
                request.getMembresia(),
                anios,
                incluirPrevios,
                fechaVencimiento,
                request.getDesarrolloConsumo(),
                usuario
        );
    }
}
