package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.CotizacionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionCredencialesResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoManualResponse;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    public List<MovimientoManualResponse> generar(GeneracionMovimientoRequest request, String usuario) {
        Map<String, Object> params = request.getParametrosEspeciales() != null
                ? request.getParametrosEspeciales() 
                : Map.of();

        Integer anios = 1;
        if (params.containsKey("anios") && params.get("anios") != null) {
            Object aniosVal = params.get("anios");
            if (aniosVal instanceof Number num) {
                anios = num.intValue();
            } else {
                anios = Integer.parseInt(aniosVal.toString().trim());
            }
        }

        logger.info(usuario, "Solicitud de generacion de credenciales a {} años para la membresia {}", anios, request.getMembresia());

        Boolean incluirPrevios = false;
        if (params.containsKey("incluyePrevios") && params.get("incluyePrevios") != null) {
            Object previosVal = params.get("incluyePrevios");
            if (previosVal instanceof Boolean bool) {
                incluirPrevios = bool;
            } else {
                incluirPrevios = Boolean.parseBoolean(previosVal.toString().trim());
            }
        } else if (params.containsKey("incluirPrevios") && params.get("incluirPrevios") != null) {
            Object previosVal = params.get("incluirPrevios");
            if (previosVal instanceof Boolean bool) {
                incluirPrevios = bool;
            } else {
                incluirPrevios = Boolean.parseBoolean(previosVal.toString().trim());
            }
        }

        LocalDate fechaVencimiento = request.getFechaVencimiento() != null 
                ? request.getFechaVencimiento() 
                : LocalDate.now();

        return repository.spCobranzaInsertaMovimientoCredenciales(
                request.getMembresia(),
                anios,
                incluirPrevios,
                fechaVencimiento.atStartOfDay(),
                request.getDesarrolloConsumo(),
                usuario
        );
    }

    @Override
    public CotizacionMovimientoResponse cotizar(CotizacionMovimientoRequest request) {
        Map<String, Object> params = request.parametrosEspeciales() != null
                ? request.parametrosEspeciales()
                : Map.of();

        Integer anios = 1;
        if (params.containsKey("anios") && params.get("anios") != null) {
            Object aniosVal = params.get("anios");
            if (aniosVal instanceof Number num) {
                anios = num.intValue();
            } else {
                anios = Integer.parseInt(aniosVal.toString().trim());
            }
        }

        Boolean incluirPrevios = false;
        if (params.containsKey("incluyePrevios") && params.get("incluyePrevios") != null) {
            Object previosVal = params.get("incluyePrevios");
            if (previosVal instanceof Boolean bool) {
                incluirPrevios = bool;
            } else {
                incluirPrevios = Boolean.parseBoolean(previosVal.toString().trim());
            }
        } else if (params.containsKey("incluirPrevios") && params.get("incluirPrevios") != null) {
            Object previosVal = params.get("incluirPrevios");
            if (previosVal instanceof Boolean bool) {
                incluirPrevios = bool;
            } else {
                incluirPrevios = Boolean.parseBoolean(previosVal.toString().trim());
            }
        }

        Integer desarrolloConsumo = request.desarrolloConsumo() != null ? request.desarrolloConsumo() : 0;

        CotizacionCredencialesResponse cot = repository.spCobranzaConsultarCotizacionCredenciales(
                request.membresia(),
                anios,
                incluirPrevios,
                desarrolloConsumo
        );

        BigDecimal subtotal = cot != null && cot.calculoTotal() != null ? cot.calculoTotal() : BigDecimal.ZERO;
        BigDecimal tarifaEstablecida = cot != null && cot.tarifaEstablecida() != null ? cot.tarifaEstablecida() : BigDecimal.ZERO;
        Integer cantidadBeneficiarios = cot != null ? cot.cantidadBeneficiarios() : 0;
        int cantidadMovimientos = 1;
        if (cot != null) {
            int aInsertar = cot.cantidadMovimientosAInsertar() != null ? cot.cantidadMovimientosAInsertar() : 0;
            int aModificar = cot.cantidadMovimientosAModificar() != null ? cot.cantidadMovimientosAModificar() : 0;
            if (aInsertar + aModificar > 0) {
                cantidadMovimientos = aInsertar + aModificar;
            }
        }

        return CotizacionMovimientoResponse.builder()
                .tipoMovimientoId(TIPO_MOVIMIENTO_CREDENCIALES)
                .descripcion("CREDENCIALES")
                .cantidadMovimientos(cantidadMovimientos)
                .baseDeCobroId(284)
                .baseDeCobro("POR BENEFICIARIO")
                .periodicidadId(301)
                .periodicidad("NO APLICA")
                .totalBeneficiarios(cantidadBeneficiarios)
                .tarifaUnitario(tarifaEstablecida)
                .anioVigenciaCuota(LocalDate.now().getYear())
                .subtotal(subtotal)
                .detalles(List.of())
                .parametrosAplicados(Map.of("anios", anios, "incluyePrevios", incluirPrevios))
                .build();
    }
}
