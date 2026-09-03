package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.clientes.repository.BeneficiariosRepository;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizacionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionMovimientoDetalleDto;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionMovimientoResponse;
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
    public static final int BASE_COBRO_POR_BENEFICIARIO = 284;
    public static final String PERIODO_ANUAL = "YEAR";

    private static final List<Integer> IDS_ESPECIALES = List.of(
            MovimientoCredencialesStrategy.TIPO_MOVIMIENTO_CREDENCIALES, // 25
            MovimientoPaqueteAnualStrategy.TIPO_MOVIMIENTO_PAQUETE_ANUAL  // 1000
    );

    private static final Pattern PATTERN_PERIODICIDAD_ANIO = Pattern.compile("(\\d+)/(\\d+)\\s+(\\d{4})");
    private static final Pattern PATTERN_ANIO = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");

    private final GeneracionMovimientosRepository repository;
    private final BeneficiariosRepository beneficiariosRepository;
    private final BusinessLogger logger;

    @Override
    public boolean soporta(Integer tipoMovimientoId) {
        return tipoMovimientoId != null && !IDS_ESPECIALES.contains(tipoMovimientoId);
    }

    @Override
    public List<MovimientoManualResponse> generar(GeneracionMovimientoRequest request, String usuario) {
        String membresia = request.getMembresia();
        Integer tipoMovimientoId = request.getTipoMovimientoId();

        Map<String, Object> params = request.getParametrosEspeciales() != null
                ? request.getParametrosEspeciales()
                : Map.of();

        MovimientoPorTipoMembresiaResponse movimientoConfig = repository.spCobranzaObtenerMovimientosPorTipoMembresia(membresia)
                .stream()
                .filter(m -> tipoMovimientoId.equals(m.id()))
                .findFirst()
                .orElse(null);

        Integer periodicidadId = movimientoConfig != null ? movimientoConfig.periodicidadId() : null;

        if (periodicidadId == null || periodicidadId == PERIODICIDAD_NO_APLICA) {
            return generarFlujoNormal(request, movimientoConfig, params, usuario);
        }

        return generarFlujoPeriodicidad(request, movimientoConfig, params, usuario);
    }

    @Override
    public CotizacionMovimientoResponse cotizar(CotizacionMovimientoRequest request) {
        String membresia = request.membresia();
        Integer tipoMovimientoId = request.tipoMovimientoId();

        Map<String, Object> params = request.parametrosEspeciales() != null
                ? request.parametrosEspeciales()
                : Map.of();

        MovimientoPorTipoMembresiaResponse movimientoConfig = repository.spCobranzaObtenerMovimientosPorTipoMembresia(membresia)
                .stream()
                .filter(m -> tipoMovimientoId.equals(m.id()))
                .findFirst()
                .orElse(null);

        Integer periodicidadId = movimientoConfig != null ? movimientoConfig.periodicidadId() : null;

        if (periodicidadId == null || periodicidadId == PERIODICIDAD_NO_APLICA) {
            return cotizarFlujoNormal(request, movimientoConfig, params);
        }

        return cotizarFlujoPeriodicidad(request, movimientoConfig, params);
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

        cuota = ajustarCuotaSegunBaseDeCobro(request.getMembresia(), cuota, movimientoConfig);

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

    private CotizacionMovimientoResponse cotizarFlujoNormal(
            CotizacionMovimientoRequest request,
            MovimientoPorTipoMembresiaResponse movimientoConfig,
            Map<String, Object> params
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

        BigDecimal cuotaUnitaria = BigDecimal.ONE;
        if (params.containsKey("cuota") && params.get("cuota") != null) {
            Object cuotaVal = params.get("cuota");
            if (cuotaVal instanceof BigDecimal bigDecimal) {
                cuotaUnitaria = bigDecimal;
            } else if (cuotaVal instanceof Number num) {
                cuotaUnitaria = BigDecimal.valueOf(num.doubleValue());
            } else {
                cuotaUnitaria = new BigDecimal(cuotaVal.toString().trim());
            }
        } else if (movimientoConfig != null && movimientoConfig.cuota() != null) {
            cuotaUnitaria = movimientoConfig.cuota();
        }

        BigDecimal cuotaAjustada = ajustarCuotaSegunBaseDeCobro(request.membresia(), cuotaUnitaria, movimientoConfig);
        BigDecimal subtotal = cuotaAjustada.multiply(BigDecimal.valueOf(cantidad));

        LocalDate fechaVencimiento = request.fechaVencimiento() != null
                ? request.fechaVencimiento()
                : LocalDate.now();

        List<CotizacionMovimientoDetalleDto> detalles = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            detalles.add(CotizacionMovimientoDetalleDto.builder()
                    .descripcion(descripcion)
                    .cuota(cuotaAjustada)
                    .fechaVencimiento(fechaVencimiento.atStartOfDay())
                    .build());
        }

        Integer baseDeCobroId = movimientoConfig != null ? movimientoConfig.baseDeCobroId() : null;
        int totalBeneficiarios = (baseDeCobroId != null && baseDeCobroId == BASE_COBRO_POR_BENEFICIARIO)
                ? contarBeneficiarios(request.membresia())
                : 0;

        return CotizacionMovimientoResponse.builder()
                .tipoMovimientoId(request.tipoMovimientoId())
                .descripcion(descripcion)
                .cantidadMovimientos(cantidad)
                .baseDeCobroId(baseDeCobroId)
                .baseDeCobro(movimientoConfig != null ? movimientoConfig.baseDeCobro() : "")
                .periodicidadId(PERIODICIDAD_NO_APLICA)
                .periodicidad("NO APLICA")
                .totalBeneficiarios(totalBeneficiarios)
                .tarifaUnitario(cuotaUnitaria)
                .anioVigenciaCuota(movimientoConfig != null ? movimientoConfig.anioVigencia() : LocalDate.now().getYear())
                .subtotal(subtotal)
                .detalles(detalles)
                .parametrosAplicados(params)
                .build();
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

        MapeoPeriodicidadResponse periodicidadMapeo = repository.spCobranzaMapeoPeriodicidad(movimientoConfig.periodicidadId(), PERIODO_ANUAL)
                .stream()
                .findFirst()
                .orElse(null);

        int periodosPorAnio = (periodicidadMapeo != null && periodicidadMapeo.cantidadXPeriodo() != null && periodicidadMapeo.cantidadXPeriodo() > 0)
                ? periodicidadMapeo.cantidadXPeriodo()
                : 1;

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

            String periodicidadValor = periodoCursor + "/" + periodosPorAnio;
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append(movimientoDesc);
            if (!periodicidadDesc.isBlank() && !movimientoDesc.toUpperCase().contains(periodicidadDesc.toUpperCase())) {
                sbDesc.append(" ").append(periodicidadDesc);
            }
            sbDesc.append(" ").append(periodicidadValor);
            sbDesc.append(" ").append(anioCursor);
            String descripcion = sbDesc.toString().replaceAll("\\s+", " ").toUpperCase().trim();

            int mesesPorPeriodo = Math.max(1, 12 / periodosPorAnio);
            int mesVencimiento = ((periodoCursor - 1) * mesesPorPeriodo) + 1;
            mesVencimiento = Math.min(Math.max(1, mesVencimiento), 12);
            LocalDate fechaVencimiento = LocalDate.of(anioCursor, mesVencimiento, 1);

            BigDecimal cuota = repository.spCobranzaObtenerTarifaMovimiento(membresia, tipoMovimientoId, anioCursor)
                    .map(TarifaMovimientoResponse::cuota)
                    .orElse(movimientoConfig.cuota() != null ? movimientoConfig.cuota() : BigDecimal.ZERO);

            cuota = ajustarCuotaSegunBaseDeCobro(membresia, cuota, movimientoConfig);

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

    private CotizacionMovimientoResponse cotizarFlujoPeriodicidad(
            CotizacionMovimientoRequest request,
            MovimientoPorTipoMembresiaResponse movimientoConfig,
            Map<String, Object> params
    ) {
        String membresia = request.membresia();
        Integer tipoMovimientoId = request.tipoMovimientoId();
        Integer desarrolloConsumo = request.desarrolloConsumo() != null ? request.desarrolloConsumo() : 0;

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

        MapeoPeriodicidadResponse periodicidadMapeo = repository.spCobranzaMapeoPeriodicidad(movimientoConfig.periodicidadId(), PERIODO_ANUAL)
                .stream()
                .findFirst()
                .orElse(null);

        int periodosPorAnio = (periodicidadMapeo != null && periodicidadMapeo.cantidadXPeriodo() != null && periodicidadMapeo.cantidadXPeriodo() > 0)
                ? periodicidadMapeo.cantidadXPeriodo()
                : 1;

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
        }

        List<CotizacionMovimientoDetalleDto> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal primeraTarifaUnitaria = null;
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

            String periodicidadValor = periodoCursor + "/" + periodosPorAnio;
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append(movimientoDesc);
            if (!periodicidadDesc.isBlank() && !movimientoDesc.toUpperCase().contains(periodicidadDesc.toUpperCase())) {
                sbDesc.append(" ").append(periodicidadDesc);
            }
            sbDesc.append(" ").append(periodicidadValor);
            sbDesc.append(" ").append(anioCursor);
            String descripcion = sbDesc.toString().replaceAll("\\s+", " ").toUpperCase().trim();

            int mesesPorPeriodo = Math.max(1, 12 / periodosPorAnio);
            int mesVencimiento = ((periodoCursor - 1) * mesesPorPeriodo) + 1;
            mesVencimiento = Math.min(Math.max(1, mesVencimiento), 12);
            LocalDate fechaVencimiento = LocalDate.of(anioCursor, mesVencimiento, 1);

            BigDecimal cuotaBase = repository.spCobranzaObtenerTarifaMovimiento(membresia, tipoMovimientoId, anioCursor)
                    .map(TarifaMovimientoResponse::cuota)
                    .orElse(movimientoConfig.cuota() != null ? movimientoConfig.cuota() : BigDecimal.ONE);

            if (primeraTarifaUnitaria == null) {
                primeraTarifaUnitaria = cuotaBase;
            }

            BigDecimal cuotaAjustada = ajustarCuotaSegunBaseDeCobro(membresia, cuotaBase, movimientoConfig);

            detalles.add(CotizacionMovimientoDetalleDto.builder()
                    .descripcion(descripcion)
                    .cuota(cuotaAjustada)
                    .fechaVencimiento(fechaVencimiento.atStartOfDay())
                    .build());

            subtotal = subtotal.add(cuotaAjustada);
        }

        Integer baseDeCobroId = movimientoConfig.baseDeCobroId();
        int totalBeneficiarios = (baseDeCobroId != null && baseDeCobroId == BASE_COBRO_POR_BENEFICIARIO)
                ? contarBeneficiarios(membresia)
                : 0;

        return CotizacionMovimientoResponse.builder()
                .tipoMovimientoId(tipoMovimientoId)
                .descripcion(movimientoDesc)
                .cantidadMovimientos(detalles.size())
                .baseDeCobroId(baseDeCobroId)
                .baseDeCobro(movimientoConfig.baseDeCobro())
                .periodicidadId(movimientoConfig.periodicidadId())
                .periodicidad(periodicidadDesc)
                .totalBeneficiarios(totalBeneficiarios)
                .tarifaUnitario(primeraTarifaUnitaria != null ? primeraTarifaUnitaria : BigDecimal.ZERO)
                .anioVigenciaCuota(anioCursor)
                .subtotal(subtotal)
                .detalles(detalles)
                .parametrosAplicados(params)
                .build();
    }

    private int contarBeneficiarios(String membresia) {
        List<?> beneficiarios = beneficiariosRepository.spClienteObtenerBeneficiariosMembresia(membresia);
        return beneficiarios != null ? beneficiarios.size() : 0;
    }

    private BigDecimal ajustarCuotaSegunBaseDeCobro(
            String membresia,
            BigDecimal cuotaBase,
            MovimientoPorTipoMembresiaResponse movimientoConfig
    ) {
        if (cuotaBase == null) {
            cuotaBase = BigDecimal.ZERO;
        }

        if (movimientoConfig != null && Integer.valueOf(BASE_COBRO_POR_BENEFICIARIO).equals(movimientoConfig.baseDeCobroId())) {
            int totalBeneficiarios = contarBeneficiarios(membresia);
            return cuotaBase.multiply(BigDecimal.valueOf(totalBeneficiarios));
        }

        return cuotaBase;
    }

    private record EstadoUltimoMovimiento(int anio, int periodoActual) {}

    private EstadoUltimoMovimiento parsearUltimoMovimiento(String descripcion, int periodosPorAnio) {
        if (descripcion == null || descripcion.isBlank()) {
            return new EstadoUltimoMovimiento(LocalDate.now().getYear(), 0);
        }

        Matcher matcherPeriodoAnio = PATTERN_PERIODICIDAD_ANIO.matcher(descripcion);
        if (matcherPeriodoAnio.find()) {
            int periodo = Integer.parseInt(matcherPeriodoAnio.group(1));
            int anio = Integer.parseInt(matcherPeriodoAnio.group(3));
            return new EstadoUltimoMovimiento(anio, periodo);
        }

        Matcher matcherAnio = PATTERN_ANIO.matcher(descripcion);
        int anioEncontrado = LocalDate.now().getYear();
        boolean encontroAnio = false;
        while (matcherAnio.find()) {
            anioEncontrado = Integer.parseInt(matcherAnio.group(1));
            encontroAnio = true;
        }

        if (encontroAnio) {
            return new EstadoUltimoMovimiento(anioEncontrado, periodosPorAnio);
        }

        return new EstadoUltimoMovimiento(LocalDate.now().getYear(), 0);
    }
}
