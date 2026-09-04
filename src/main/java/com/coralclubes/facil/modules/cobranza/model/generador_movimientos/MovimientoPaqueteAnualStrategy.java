package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.CotizacionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionMovimientoDetalleDto;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoManualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.TarifaMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.UltimoMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MovimientoPaqueteAnualStrategy implements GeneracionMovimientoStrategy {

    public static final int TIPO_MOVIMIENTO_PAQUETE_ANUAL = 1000;
    public static final BigDecimal CUOTA_DEFAULT = new BigDecimal("1.00");

    private static final Pattern PATTERN_ANIO_PAQUETE = Pattern.compile("PAQUETE\\s+ANUAL.*?(\\b(?:19|20)\\d{2}\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ANIO_GENERICO = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");

    private final GeneracionMovimientosRepository repository;
    private final BusinessLogger logger;

    @Override
    public boolean soporta(Integer tipoMovimientoId) {
        return tipoMovimientoId != null && tipoMovimientoId == TIPO_MOVIMIENTO_PAQUETE_ANUAL;
    }

    @Override
    public List<MovimientoManualResponse> generar(GeneracionMovimientoRequest request, String usuario) {
        String membresia = request.getMembresia();
        Integer desarrolloConsumo = request.getDesarrolloConsumo() != null ? request.getDesarrolloConsumo() : 0;

        Map<String, Object> params = request.getParametrosEspeciales() != null
                ? request.getParametrosEspeciales()
                : Map.of();

        Integer anio = extraerAnio(params);
        String procedenciaLabel = extraerProcedenciaLabel(params);

        // 1. Obtener el último movimiento de paquete anual
        UltimoMovimientoResponse ultimoMovimiento = repository.spCobranzaObtenerUltimoMovimiento(
                membresia,
                desarrolloConsumo,
                TIPO_MOVIMIENTO_PAQUETE_ANUAL,
                null
        ).orElse(null);

        // 2. Validar que no esté ya generado para ese año
        if (ultimoMovimiento != null && ultimoMovimiento.descripcion() != null) {
            Integer anioUltimo = extraerAnioUltimoMovimiento(ultimoMovimiento.descripcion());
            if (anioUltimo != null && anioUltimo.equals(anio)) {
                throw new IllegalArgumentException("Ya existe un movimiento de Paquete Anual generado para el año " + anio + " en la membresía: " + membresia);
            }
        }

        // Estructura de descripción: PAQUETE ANUAL + 1/1 + AÑO + PROCEDENCIA + LABEL
        String descripcion = ("PAQUETE ANUAL 1/1 " + anio + " PROCEDENCIA " + procedenciaLabel).toUpperCase().trim();

        BigDecimal cuota = resolverCuota(membresia, anio, params);

        LocalDate fechaVencimiento = request.getFechaVencimiento() != null
                ? request.getFechaVencimiento()
                : LocalDate.of(anio, 1, 1);

        logger.info(usuario, "Generando movimiento de Paquete Anual para membresía {}: Descripción='{}', Cuota={}, Vencimiento={}",
                membresia, descripcion, cuota, fechaVencimiento);

        return repository.spCobranzaInsertaMovimientoManual(
                membresia,
                TIPO_MOVIMIENTO_PAQUETE_ANUAL,
                1,
                descripcion,
                cuota,
                fechaVencimiento.atStartOfDay(),
                desarrolloConsumo,
                usuario
        );
    }

    @Override
    public CotizacionMovimientoResponse cotizar(CotizacionMovimientoRequest request) {
        String membresia = request.membresia();
        Integer desarrolloConsumo = request.desarrolloConsumo() != null ? request.desarrolloConsumo() : 0;

        Map<String, Object> params = request.parametrosEspeciales() != null
                ? request.parametrosEspeciales()
                : Map.of();

        Integer anio = extraerAnio(params);
        String procedenciaLabel = extraerProcedenciaLabel(params);

        // Validar si ya existe el movimiento para el año
        UltimoMovimientoResponse ultimoMovimiento = repository.spCobranzaObtenerUltimoMovimiento(
                membresia,
                desarrolloConsumo,
                TIPO_MOVIMIENTO_PAQUETE_ANUAL,
                null
        ).orElse(null);

        if (ultimoMovimiento != null && ultimoMovimiento.descripcion() != null) {
            Integer anioUltimo = extraerAnioUltimoMovimiento(ultimoMovimiento.descripcion());
            if (anioUltimo != null && anioUltimo.equals(anio)) {
                throw new IllegalArgumentException("El año " + anio + " ya cuenta con un movimiento de Paquete Anual registrado para la membresía: " + membresia);
            }
        }

        String descripcion = ("PAQUETE ANUAL 1/1 " + anio + " PROCEDENCIA " + procedenciaLabel).toUpperCase().trim();
        BigDecimal cuota = resolverCuota(membresia, anio, params);

        LocalDate fechaVencimiento = request.fechaVencimiento() != null
                ? request.fechaVencimiento()
                : LocalDate.of(anio, 1, 1);

        CotizacionMovimientoDetalleDto detalle = CotizacionMovimientoDetalleDto.builder()
                .descripcion(descripcion)
                .cuota(cuota)
                .fechaVencimiento(fechaVencimiento.atStartOfDay())
                .build();

        return CotizacionMovimientoResponse.builder()
                .tipoMovimientoId(TIPO_MOVIMIENTO_PAQUETE_ANUAL)
                .descripcion(descripcion)
                .cantidadMovimientos(1)
                .baseDeCobroId(301)
                .baseDeCobro("NO APLICA")
                .periodicidadId(301)
                .periodicidad("ANUAL")
                .totalBeneficiarios(0)
                .tarifaUnitario(cuota)
                .anioVigenciaCuota(anio)
                .subtotal(cuota)
                .detalles(List.of(detalle))
                .parametrosAplicados(params)
                .build();
    }

    private Integer extraerAnio(Map<String, Object> params) {
        if (params.containsKey("anio") && params.get("anio") != null) {
            Object anioVal = params.get("anio");
            if (anioVal instanceof Number num) {
                return num.intValue();
            }
            return Integer.parseInt(anioVal.toString().trim());
        }
        if (params.containsKey("anioCompleto") && params.get("anioCompleto") != null) {
            Object anioVal = params.get("anioCompleto");
            if (anioVal instanceof Number num) {
                return num.intValue();
            }
            return Integer.parseInt(anioVal.toString().trim());
        }
        if (params.containsKey("year") && params.get("year") != null) {
            Object anioVal = params.get("year");
            if (anioVal instanceof Number num) {
                return num.intValue();
            }
            return Integer.parseInt(anioVal.toString().trim());
        }
        return LocalDate.now().getYear();
    }

    private String extraerProcedenciaLabel(Map<String, Object> params) {
        if (params.containsKey("procedenciaLabel") && params.get("procedenciaLabel") != null && !params.get("procedenciaLabel").toString().isBlank()) {
            return params.get("procedenciaLabel").toString().trim();
        }
        if (params.containsKey("labelProcedencia") && params.get("labelProcedencia") != null && !params.get("labelProcedencia").toString().isBlank()) {
            return params.get("labelProcedencia").toString().trim();
        }
        if (params.containsKey("procedencia") && params.get("procedencia") != null && !params.get("procedencia").toString().isBlank()) {
            return params.get("procedencia").toString().trim();
        }
        return "VENTAS";
    }

    private BigDecimal resolverCuota(String membresia, Integer anio, Map<String, Object> params) {
        if (params.containsKey("cuota") && params.get("cuota") != null) {
            Object cuotaVal = params.get("cuota");
            if (cuotaVal instanceof BigDecimal bigDecimal && bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                return bigDecimal;
            }
            if (cuotaVal instanceof Number num && num.doubleValue() > 0) {
                return BigDecimal.valueOf(num.doubleValue());
            }
            try {
                BigDecimal parsed = new BigDecimal(cuotaVal.toString().trim());
                if (parsed.compareTo(BigDecimal.ZERO) > 0) {
                    return parsed;
                }
            } catch (Exception ignored) {}
        }

        return repository.spCobranzaObtenerTarifaMovimiento(membresia, TIPO_MOVIMIENTO_PAQUETE_ANUAL, anio)
                .map(TarifaMovimientoResponse::cuota)
                .filter(c -> c.compareTo(BigDecimal.ZERO) > 0)
                .orElse(CUOTA_DEFAULT);
    }

    private Integer extraerAnioUltimoMovimiento(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        Matcher matcher = PATTERN_ANIO_PAQUETE.matcher(descripcion);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        Matcher matcherGen = PATTERN_ANIO_GENERICO.matcher(descripcion);
        Integer ultimoAnio = null;
        while (matcherGen.find()) {
            ultimoAnio = Integer.parseInt(matcherGen.group(1));
        }
        return ultimoAnio;
    }
}
