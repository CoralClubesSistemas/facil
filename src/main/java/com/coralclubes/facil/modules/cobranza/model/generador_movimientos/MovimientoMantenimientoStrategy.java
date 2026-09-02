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
public class MovimientoMantenimientoStrategy implements GeneracionMovimientoStrategy {

    public static final int MOVIMIENTO_MANTENIMIENTO = 10;
    public static final String PERIODO_ANUAL = "YEAR";

    private static final Pattern PATTERN_PERIODICIDAD_ANIO = Pattern.compile("(\\d+)/(\\d+)\\s+(\\d{4})");
    private static final Pattern PATTERN_ANIO = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");

    private final GeneracionMovimientosRepository repository;
    private final BusinessLogger logger;

    @Override
    public boolean soporta(Integer tipoMovimientoId) {
        return tipoMovimientoId != null && tipoMovimientoId == MOVIMIENTO_MANTENIMIENTO;
    }

    @Override
    public List<MovimientoManualResponse> generar(GeneracionMovimientoRequest request, String usuario) {
        String membresia = request.getMembresia();
        Integer desarrolloConsumo = request.getDesarrolloConsumo() != null ? request.getDesarrolloConsumo() : 0;

        Map<String, Object> params = request.getParametrosEspeciales() != null
                ? request.getParametrosEspeciales()
                : Map.of();

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

        // 1. Obtener detalles y configuración del tipo de movimiento
        MovimientoPorTipoMembresiaResponse tipoMovConfig = repository.spCobranzaObtenerMovimientosPorTipoMembresia(membresia)
                .stream()
                .filter(m -> MOVIMIENTO_MANTENIMIENTO == m.id())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un movimiento de mantenimiento para la membresía: " + membresia
                ));

        String tipoMovimientoDesc = tipoMovConfig.descripcion() != null && !tipoMovConfig.descripcion().isBlank()
                ? tipoMovConfig.descripcion().trim()
                : "MANTENIMIENTO";

        // 2. Obtener mapeo de periodicidad (ej. Anual = 1, Semestral = 2, Trimestral = 4, etc.)
        MapeoPeriodicidadResponse periodicidadMapeo = repository.spCobranzaMapeoPeriodicidad(tipoMovConfig.periodicidadId(), PERIODO_ANUAL)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un mapeo de periodicidad para la membresía: " + membresia
                ));

        int periodosPorAnio = periodicidadMapeo.cantidadXPeriodo() != null && periodicidadMapeo.cantidadXPeriodo() > 0
                ? periodicidadMapeo.cantidadXPeriodo()
                : 1;

        // 3. Obtener el último movimiento de mantenimiento generado
        UltimoMovimientoResponse ultimoMantenimiento = repository.spCobranzaObtenerUltimoMovimiento(
                membresia,
                desarrolloConsumo,
                MOVIMIENTO_MANTENIMIENTO,
                null
        ).orElse(null);

        // 4. Determinar año y periodicidad inicial
        int anioCursor = LocalDate.now().getYear();
        int periodoCursor = 0;

        if (ultimoMantenimiento != null && ultimoMantenimiento.descripcion() != null && !ultimoMantenimiento.descripcion().isBlank()) {
            EstadoUltimoMovimiento estado = parsearUltimoMovimiento(ultimoMantenimiento.descripcion(), periodosPorAnio);
            anioCursor = estado.anio();
            periodoCursor = estado.periodoActual();
        }

        if (anioCompleto != null) {
            if (anioCursor > anioCompleto || (anioCursor == anioCompleto && periodoCursor >= periodosPorAnio)) {
                throw new IllegalArgumentException("El año " + anioCompleto + " ya se encuentra completamente cubierto para la membresía: " + membresia);
            }
            logger.info(usuario, "Iniciando generación completa hasta el año {} para {}. Estado previo: Año={}, Periodo={}/{}",
                    anioCompleto, membresia, anioCursor, periodoCursor, periodosPorAnio);
        } else {
            logger.info(usuario, "Iniciando generación de {} movimiento(s) de mantenimiento para {}. Estado previo: Año={}, Periodo={}/{}",
                    cantidadAGenerar, membresia, anioCursor, periodoCursor, periodosPorAnio);
        }

        List<MovimientoManualResponse> movimientosGenerados = new ArrayList<>();
        int contador = 0;

        // 5. Generar los movimientos necesarios
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

            // Descripción: tipo de movimiento + periodicidad + valor_periodicidad + año
            String periodicidadTexto = periodoCursor + "/" + periodosPorAnio;
            String periodicidadDesc = tipoMovConfig.periodicidad() != null && !tipoMovConfig.periodicidad().isBlank()
                    ? tipoMovConfig.periodicidad().trim()
                    : "PERIODO";
            String descripcion = (tipoMovimientoDesc + " " + periodicidadDesc + " " + periodicidadTexto + " " + anioCursor).toUpperCase().trim();

            // Fecha de vencimiento: se dividen los meses del año entre la periodicidad y es el día 1 del mes correspondiente
            int mesesPorPeriodo = Math.max(1, 12 / periodosPorAnio);
            int mesVencimiento = ((periodoCursor - 1) * mesesPorPeriodo) + 1;
            mesVencimiento = Math.min(Math.max(1, mesVencimiento), 12);
            LocalDate fechaVencimiento = LocalDate.of(anioCursor, mesVencimiento, 1);

            // Obtener tarifa/cuota aplicable para el año correspondiente
            BigDecimal cuota = repository.spCobranzaObtenerTarifaMovimiento(membresia, MOVIMIENTO_MANTENIMIENTO, anioCursor)
                    .map(TarifaMovimientoResponse::cuota)
                    .orElse(tipoMovConfig.cuota() != null ? tipoMovConfig.cuota() : BigDecimal.ZERO);

            logger.info(usuario, "Generando movimiento mantenimiento #{}: Descripción='{}', Cuota={}, Vencimiento={}",
                    contador, descripcion, cuota, fechaVencimiento);

            List<MovimientoManualResponse> insertados = repository.spCobranzaInsertaMovimientoManual(
                    membresia,
                    MOVIMIENTO_MANTENIMIENTO,
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

        // Si no tiene "X/Y" buscar el año en la descripción
        Matcher matcherAnio = PATTERN_ANIO.matcher(descripcion);
        int anioEncontrado = LocalDate.now().getYear();
        while (matcherAnio.find()) {
            anioEncontrado = Integer.parseInt(matcherAnio.group(1));
        }

        // Si solo se encontró el año, se asume que se completaron los periodos de ese año
        return new EstadoUltimoMovimiento(anioEncontrado, periodosPorAnio);
    }
}
