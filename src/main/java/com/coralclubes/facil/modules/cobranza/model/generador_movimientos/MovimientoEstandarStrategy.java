package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.MapeoPeriodicidadResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoManualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.TarifaMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.UltimoMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MovimientoEstandarStrategy implements GeneracionMovimientoStrategy {

    public static final int PERIODICIDAD_NO_APLICA = 301;
    public static final String PERIODO_ANUAL = "YEAR";

    private static final List<Integer> IDS_ESPECIALES = List.of(
            MovimientoCredencialesStrategy.TIPO_MOVIMIENTO_CREDENCIALES // 25
    );

    private static final Pattern PATTERN_PERIODICIDAD_ANIO = Pattern.compile("(\\d+)/(\\d+)\\s+(\\d{4})");
    private static final Pattern PATTERN_ANIO = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");

    private final GeneracionMovimientosRepository repository;
    private final BusinessLogger logger;

    @Override
    public boolean soporta(Integer tipoMovimientoId) {
        return tipoMovimientoId != null && !IDS_ESPECIALES.contains(tipoMovimientoId);
    }

    @Override
    public List<MovimientoManualResponse> generar(GeneracionMovimientoRequest request, String usuario) {
        String membresia = request.getMembresia();
        Integer tipoMovimientoId = request.getTipoMovimientoId();
        Integer desarrolloConsumo = request.getDesarrolloConsumo() != null ? request.getDesarrolloConsumo() : 0;

        Map<String, Object> params = request.getParametrosEspeciales() != null
                ? request.getParametrosEspeciales()
                : Map.of();

        // 1. Buscar configuración del movimiento para la membresía
        MovimientoPorTipoMembresiaResponse movimientoConfig = repository.spCobranzaObtenerMovimientosPorTipoMembresia(membresia)
                .stream()
                .filter(m -> tipoMovimientoId.equals(m.id()))
                .findFirst()
                .orElse(null);

        Integer periodicidadId = movimientoConfig != null ? movimientoConfig.periodicidadId() : null;

        // Si la periodicidad es "NO APLICA" (ID 301) o no tiene periodicidad configurada, flujo normal
        if (periodicidadId == null || periodicidadId == PERIODICIDAD_NO_APLICA) {
            return generarFlujoNormal(request, movimientoConfig, params, usuario);
        }

        // Si tiene periodicidad configurada diferente de "NO APLICA", aplicar lógica de periodicidad
        return generarFlujoPeriodicidad(request, movimientoConfig, params, usuario);
    }

    private List<MovimientoManualResponse> generarFlujoNormal(
            GeneracionMovimientoRequest request,
            MovimientoPorTipoMembresiaResponse movimientoConfig,
            Map<String, Object> params,
            String usuario
    ) {
        int cantidad = 1;
        if (params.containsKey("cantidadMovimientos") && params.get("cantidadMovimientos") != null) {
            Object cantVal = params.get("cantidadMovimientos");
            if (cantVal instanceof Number num) {
                cantidad = num.intValue();
            } else {
                cantidad = Integer.parseInt(cantVal.toString().trim());
            }
        }

        String descripcion = "";
        if (params.containsKey("descripcion") && params.get("descripcion") != null) {
            descripcion = params.get("descripcion").toString();
        } else if (movimientoConfig != null && movimientoConfig.descripcion() != null) {
            descripcion = movimientoConfig.descripcion();
        }

        BigDecimal cuota = BigDecimal.ZERO;
        if (params.containsKey("cuota") && params.get("cuota") != null) {
            Object cuotaVal = params.get("cuota");
            if (cuotaVal instanceof BigDecimal bigDecimal) {
                cuota = bigDecimal;
            } else if (cuotaVal instanceof Number num) {
                cuota = BigDecimal.valueOf(num.doubleValue());
            } else {
                cuota = new BigDecimal(cuotaVal.toString().trim());
            }
        } else if (movimientoConfig != null && movimientoConfig.cuota() != null) {
            cuota = movimientoConfig.cuota();
        }

        LocalDate fechaVencimiento = request.getFechaVencimiento() != null
                ? request.getFechaVencimiento()
                : LocalDate.now();

        logger.info(usuario, "Generando movimiento estándar sin periodicidad: {}", Map.of(
                "membresia", request.getMembresia(),
                "tipoMovimientoId", request.getTipoMovimientoId(),
                "cantidad", cantidad,
                "descripcion", descripcion,
                "cuota", cuota,
                "fechaVencimiento", fechaVencimiento,
                "desarrolloConsumo", request.getDesarrolloConsumo() != null ? request.getDesarrolloConsumo() : 0
        ));

        return repository.spCobranzaInsertaMovimientoManual(
                request.getMembresia(),
                request.getTipoMovimientoId(),
                cantidad,
                descripcion,
                cuota,
                fechaVencimiento.atStartOfDay(),
                request.getDesarrolloConsumo() != null ? request.getDesarrolloConsumo() : 0,
                usuario
        );
    }

    private List<MovimientoManualResponse> generarFlujoPeriodicidad(
            GeneracionMovimientoRequest request,
            MovimientoPorTipoMembresiaResponse movimientoConfig,
            Map<String, Object> params,
            String usuario
    ) {
        String membresia = request.getMembresia();
        Integer tipoMovimientoId = request.getTipoMovimientoId();
        Integer desarrolloConsumo = request.getDesarrolloConsumo() != null ? request.getDesarrolloConsumo() : 0;

        Integer anioCompleto = null;
        if (params.containsKey("anioCompleto") && params.get("anioCompleto") != null) {
            Object anioVal = params.get("anioCompleto");
            if (anioVal instanceof Number num) {
                anioCompleto = num.intValue();
            } else {
                anioCompleto = Integer.parseInt(anioVal.toString().trim());
            }
        }

        int cantidadAGenerar = 1;
        if (params.containsKey("cantidadMovimientos") && params.get("cantidadMovimientos") != null) {
            Object cantVal = params.get("cantidadMovimientos");
            if (cantVal instanceof Number num) {
                cantidadAGenerar = num.intValue();
            } else {
                cantidadAGenerar = Integer.parseInt(cantVal.toString().trim());
            }
        }

        String movimientoDesc = movimientoConfig.descripcion() != null && !movimientoConfig.descripcion().isBlank()
                ? movimientoConfig.descripcion().trim()
                : "";

        String periodicidadDesc = movimientoConfig.periodicidad() != null && !movimientoConfig.periodicidad().isBlank()
                ? movimientoConfig.periodicidad().trim()
                : "";

        // 1. Obtener mapeo de periodicidad
        MapeoPeriodicidadResponse periodicidadMapeo = repository.spCobranzaMapeoPeriodicidad(movimientoConfig.periodicidadId(), PERIODO_ANUAL)
                .stream()
                .findFirst()
                .orElse(null);

        int periodosPorAnio = (periodicidadMapeo != null && periodicidadMapeo.cantidadXPeriodo() != null && periodicidadMapeo.cantidadXPeriodo() > 0)
                ? periodicidadMapeo.cantidadXPeriodo()
                : 1;

        // 0. Obtener último movimiento
        UltimoMovimientoResponse ultimoMovimiento = repository.spCobranzaObtenerUltimoMovimiento(
                membresia,
                desarrolloConsumo,
                tipoMovimientoId,
                null
        ).orElse(null);

        int anioCursor = LocalDate.now().getYear();
        int periodoCursor = 0;

        if (ultimoMovimiento != null && ultimoMovimiento.descripcion() != null && !ultimoMovimiento.descripcion().isBlank()) {
            EstadoUltimoMovimiento estado = parsearUltimoMovimiento(ultimoMovimiento.descripcion(), periodosPorAnio);
            anioCursor = estado.anio();
            periodoCursor = estado.periodoActual();
        }

        if (anioCompleto != null) {
            if (anioCursor > anioCompleto || (anioCursor == anioCompleto && periodoCursor >= periodosPorAnio)) {
                throw new IllegalArgumentException("El año " + anioCompleto + " ya se encuentra completamente cubierto para la membresía: " + membresia);
            }
            logger.info(usuario, "Iniciando generación periódica hasta el año {} para tipoMovimiento {} en membresía {}. Estado previo: Año={}, Periodo={}/{}",
                    anioCompleto, tipoMovimientoId, membresia, anioCursor, periodoCursor, periodosPorAnio);
        } else {
            logger.info(usuario, "Iniciando generación de {} movimiento(s) periódicos para tipoMovimiento {} en membresía {}. Estado previo: Año={}, Periodo={}/{}",
                    cantidadAGenerar, tipoMovimientoId, membresia, anioCursor, periodoCursor, periodosPorAnio);
        }

        List<MovimientoManualResponse> movimientosGenerados = new ArrayList<>();
        int contador = 0;

        while (true) {
            if (anioCompleto != null) {
                if (anioCursor > anioCompleto || (anioCursor == anioCompleto && periodoCursor >= periodosPorAnio)) {
                    break;
                }
            } else {
                if (contador >= cantidadAGenerar) {
                    break;
                }
            }

            if (periodoCursor >= periodosPorAnio) {
                anioCursor++;
                periodoCursor = 1;
            } else {
                periodoCursor++;
            }

            contador++;

            // 4. Descripción: MOVIMIENTO + PERIODICIDAD DESCRIPCIÓN + PERIODICIDAD VALOR + AÑO
            String periodicidadValor = periodoCursor + "/" + periodosPorAnio;
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append(movimientoDesc);
            if (!periodicidadDesc.isBlank() && !movimientoDesc.toUpperCase().contains(periodicidadDesc.toUpperCase())) {
                sbDesc.append(" ").append(periodicidadDesc);
            }
            sbDesc.append(" ").append(periodicidadValor);
            sbDesc.append(" ").append(anioCursor);
            String descripcion = sbDesc.toString().replaceAll("\\s+", " ").toUpperCase().trim();

            // 2 y 3. Fecha de vencimiento: 1er día de la periodicidad establecida (12 / periodosPorAnio)
            int mesesPorPeriodo = Math.max(1, 12 / periodosPorAnio);
            int mesVencimiento = ((periodoCursor - 1) * mesesPorPeriodo) + 1;
            mesVencimiento = Math.min(Math.max(1, mesVencimiento), 12);
            LocalDate fechaVencimiento = LocalDate.of(anioCursor, mesVencimiento, 1);

            // 5. Cuota dependiendo del año obtenido
            BigDecimal cuota = repository.spCobranzaObtenerTarifaMovimiento(membresia, tipoMovimientoId, anioCursor)
                    .map(TarifaMovimientoResponse::cuota)
                    .orElse(movimientoConfig.cuota() != null ? movimientoConfig.cuota() : BigDecimal.ZERO);

            logger.info(usuario, "Generando movimiento periódico #{}: Descripción='{}', Cuota={}, Vencimiento={}",
                    contador, descripcion, cuota, fechaVencimiento);

            List<MovimientoManualResponse> insertados = repository.spCobranzaInsertaMovimientoManual(
                    membresia,
                    tipoMovimientoId,
                    1,
                    descripcion,
                    cuota,
                    fechaVencimiento.atStartOfDay(),
                    desarrolloConsumo,
                    usuario
            );

            if (insertados != null) {
                movimientosGenerados.addAll(insertados);
            }
        }

        return movimientosGenerados;
    }

    private record EstadoUltimoMovimiento(int anio, int periodoActual) {
    }

    private EstadoUltimoMovimiento parsearUltimoMovimiento(String descripcion, int periodosPorAnio) {
        if (descripcion == null || descripcion.isBlank()) {
            return new EstadoUltimoMovimiento(LocalDate.now().getYear(), 0);
        }

        // Buscar patrón tipo "1/4 2026" o "2/2 2025"
        Matcher matcherPeriodoAnio = PATTERN_PERIODICIDAD_ANIO.matcher(descripcion);
        if (matcherPeriodoAnio.find()) {
            int periodo = Integer.parseInt(matcherPeriodoAnio.group(1));
            int anio = Integer.parseInt(matcherPeriodoAnio.group(3));
            return new EstadoUltimoMovimiento(anio, periodo);
        }

        // Si no tiene "X/Y" (por ejemplo movimientos anuales sin 1/1), buscamos el año
        Matcher matcherAnio = PATTERN_ANIO.matcher(descripcion);
        int anioEncontrado = LocalDate.now().getYear();
        boolean encontroAnio = false;
        while (matcherAnio.find()) {
            anioEncontrado = Integer.parseInt(matcherAnio.group(1));
            encontroAnio = true;
        }

        // Si solo se encontró el año, se toma en cuenta que ya se generó el movimiento de ese año obtenido
        if (encontroAnio) {
            return new EstadoUltimoMovimiento(anioEncontrado, periodosPorAnio);
        }

        return new EstadoUltimoMovimiento(LocalDate.now().getYear(), 0);
    }
}
