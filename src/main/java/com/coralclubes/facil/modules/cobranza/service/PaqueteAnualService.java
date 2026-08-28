package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizarPropuestaMovimientoParamDto;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizarPropuestaPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.repository.PaqueteAnualRepository;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaqueteAnualService {

    private static final int MOVIMIENTO_CREDENCIALES = 25;
    private static final String ESQUEMA_ADICIONAL = "ADICIONAL";
    private static final int BASE_BENEFICIARIO = 284;

    private final PaqueteAnualRepository repository;
    private final GeneracionMovimientosService generacionMovimientosService;
    private final SociosService sociosService;
    private final BusinessLogger businessLogger;
    private final ObjectMapper objectMapper;

    public List<MovimientoPaqueteAnualResponse> obtenerMovimientosPaqueteAnual(Integer anio, Integer tipoMembresia) {
        return repository.spCobranzaCatalogoMovimientosPaqueteAnual(anio, tipoMembresia);
    }

    public List<PaqueteAnualResponse> obtenerPaquetesAnuales(
            Integer anio,
            Integer tipoMembresia,
            Integer clasificacionMembresia,
            Integer desarrollo
    ) {
        return repository.spCobranzaObtenerPaquetesAnuales(anio, tipoMembresia, clasificacionMembresia, desarrollo);
    }

    public PaqueteAnualDetalleResponse obtenerPaqueteAnualDetalle(Integer paqueteAnualId) {
        String jsonDetalle = repository.spCobranzaObtenerPaqueteAnualDetalle(paqueteAnualId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paquete anual con ID: " + paqueteAnualId));

        try {
            return objectMapper.readValue(jsonDetalle, PaqueteAnualDetalleResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar el detalle del paquete anual", e);
        }
    }

    public List<PeriodicidadMantenimientoResponse> obtenerPeriodicidadMantenimiento(String membresia) {
        return repository.spCobranzaObtenerPeriodicidadMantenimiento(membresia);
    }

    public List<CuponBeneficioPaqueteAnualResponse> obtenerCuponesBeneficio(String membresia, Integer anio, java.time.LocalDateTime fechaCotizacion) {
        return repository.spCobranzaObtenerCuponesBeneficioPaqueteAnual(membresia, anio, fechaCotizacion);
    }

    public List<EsquemaPagoPropuestaResponse> obtenerEsquemasPagoPropuesta(String membresia, Integer anio) {
        return repository.spCobranzaObtenerEsquemasPagoPropuestaPaqueteAnual(membresia, anio);
    }

    public CotizacionPaqueteAnualResponse cotizarPropuestaPaqueteAnual(CotizarPropuestaPaqueteAnualRequest request) {
        // Mapeo inicial de parámetros del request
        String membresia = request.membresia();
        Integer anio = request.anio();
        List<String> esquemasSeleccionados = request.esquemas() != null ? request.esquemas() : Collections.emptyList();
        List<CotizarPropuestaMovimientoParamDto> movimientosParams = request.movimientos() != null ? request.movimientos() : Collections.emptyList();

        // 1. Validar regla de esquemas combinables (solo un esquema base + opcional 'ADICIONAL')
        long esquemasBaseCount = esquemasSeleccionados.stream()
                .filter(e -> !ESQUEMA_ADICIONAL.equalsIgnoreCase(e))
                .count();

        if (esquemasBaseCount > 1) {
            throw new IllegalArgumentException("Regla inválida: Sólo se permite un esquema de pago base a la vez. Únicamente el esquema ADICIONAL puede combinarse.");
        }

        // 2. Obtener el paquete anual activo para la membresía y año
        Integer paqueteId = repository.spCobranzaObtenerPaqueteAnualActivoIdPorMembresia(membresia, anio)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una configuración de paquete anual activa para la membresía " + membresia + " y año " + anio));

        PaqueteAnualDetalleResponse paqueteDetalle = obtenerPaqueteAnualDetalle(paqueteId);

        // 3. Consultar datos del socio (desarrollo y tipo de membresía)
        ApiResponse<InformacionSocio> socioResponse = sociosService.obtenerSocios(membresia);
        InformacionSocio socio = socioResponse != null ? socioResponse.data() : null;
        Integer desarrolloId = socio != null ? socio.desarrolloId() : paqueteDetalle.desarrolloId();
        Integer tipoMembresiaId = socio != null ? socio.tipoMembresiaId() : paqueteDetalle.tipoMembresiaId();

        // 4. Obtener catálogo de movimientos para paquete anual (contiene aplicaPeriodicidad, baseDeCobroId, cuota, etc.)
        List<MovimientoPaqueteAnualResponse> catalogoMovimientos = repository.spCobranzaCatalogoMovimientosPaqueteAnual(anio, tipoMembresiaId);
        Map<Integer, MovimientoPaqueteAnualResponse> catalogoMap = catalogoMovimientos.stream()
                .collect(java.util.stream.Collectors.toMap(MovimientoPaqueteAnualResponse::id, m -> m, (m1, m2) -> m1));

        // 5. Consultar total de beneficiarios computables (activos o bloqueados)
        Integer totalBeneficiarios = repository.spCobranzaObtenerBeneficiariosPaqueteAnual(membresia);

        // 6. Consultar periodicidad de mantenimiento para la membresía
        List<PeriodicidadMantenimientoResponse> periodicidades = repository.spCobranzaObtenerPeriodicidadMantenimiento(membresia);
        PeriodicidadMantenimientoResponse periodicidadSocio = periodicidades.isEmpty() ? null : periodicidades.getFirst();

        // 7. Determinar esquemas aplicados y sumar el porcentaje total de descuento
        List<PaqueteAnualDescuentoResponse> esquemasConfigurados = paqueteDetalle.configuracionDescuentos() != null
                ? paqueteDetalle.configuracionDescuentos()
                : Collections.emptyList();

        List<PaqueteAnualDescuentoResponse> esquemasAplicados = new ArrayList<>();
        BigDecimal porcentajeDescuentoTotal = BigDecimal.ZERO;

        for (String esquemaKey : esquemasSeleccionados) {
            esquemasConfigurados.stream()
                    .filter(d -> d.value() != null && d.value().equalsIgnoreCase(esquemaKey))
                    .findFirst()
                    .ifPresent(d -> {
                        esquemasAplicados.add(d);
                    });
        }

        for (PaqueteAnualDescuentoResponse d : esquemasAplicados) {
            if (d.descuento() != null) {
                porcentajeDescuentoTotal = porcentajeDescuentoTotal.add(d.descuento());
            }
        }

        // 8. Procesar y calcular cada movimiento configurado en el paquete anual
        List<PaqueteAnualMovimientoResponse> movimientosConfigurados = paqueteDetalle.movimientos() != null
                ? paqueteDetalle.movimientos()
                : Collections.emptyList();

        List<CotizacionPaqueteAnualMovimientoResponse> movimientosCotizados = new ArrayList<>();
        BigDecimal subtotalGeneral = BigDecimal.ZERO;
        BigDecimal descuentoGeneral = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;

        // Map de parámetros dinámicos recibidos del front
        Map<Integer, Map<String, Object>> paramsPorMovimiento = movimientosParams.stream()
                .filter(p -> p.movimientoId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        CotizarPropuestaMovimientoParamDto::movimientoId,
                        p -> p.configuracionAdicional() != null ? p.configuracionAdicional() : Collections.emptyMap(),
                        (p1, p2) -> p1
                ));

        for (PaqueteAnualMovimientoResponse pam : movimientosConfigurados) {
            Integer movId = pam.movimientoId();
            MovimientoPaqueteAnualResponse catalogoItem = catalogoMap.get(movId);

            boolean aplicaPeriodicidad = catalogoItem != null && Boolean.TRUE.equals(catalogoItem.aplicaPeriodicidad());
            Integer baseDeCobroId = catalogoItem != null ? catalogoItem.baseDeCobroId() : null;
            String baseDeCobro = catalogoItem != null ? catalogoItem.baseDeCobro() : "";
            BigDecimal tarifaUnitario = (pam.cuotaVigente() != null)
                    ? pam.cuotaVigente()
                    : (catalogoItem != null && catalogoItem.cuota() != null ? catalogoItem.cuota() : BigDecimal.ZERO);
            Integer anioVigenciaCuota = (pam.anioVigenciaCuota() != null)
                    ? pam.anioVigenciaCuota()
                    : (catalogoItem != null ? catalogoItem.anioVigencia() : null);

            // Calcular cantidad de movimientos (por periodicidad o 1 por defecto)
            int cantidadMovimientos = 1;
            String periodicidadTexto = "";
            if (aplicaPeriodicidad && periodicidadSocio != null) {
                if (periodicidadSocio.cantidadPorPeriodo() != null && periodicidadSocio.cantidadPorPeriodo() > 0) {
                    cantidadMovimientos = periodicidadSocio.cantidadPorPeriodo();
                }
                periodicidadTexto = periodicidadSocio.periodicidad() != null ? periodicidadSocio.periodicidad() : "";
            }

            // Evaluar base de cobro por beneficiario nombre BENEFICIARIO
            boolean esBaseBeneficiario = baseDeCobroId != null && baseDeCobroId == BASE_BENEFICIARIO;
            int totalBeneficiariosMov = esBaseBeneficiario ? (totalBeneficiarios != null ? totalBeneficiarios : 0) : 0;

            // Obtener configuración adicional (del request o del paquete)
            Map<String, Object> configAdicional = paramsPorMovimiento.containsKey(movId)
                    ? paramsPorMovimiento.get(movId)
                    : pam.configuracionAdicional();

            BigDecimal subtotal;
            BigDecimal montoDescuento;
            BigDecimal total;

            if (movId != null && movId == MOVIMIENTO_CREDENCIALES) {
                // Caso especial: CREDENCIALES -> Llama al servicio de cotizar credenciales
                int aniosCredencial = 1;
                boolean incluirPrevios = false;
                if (configAdicional != null && configAdicional.containsKey("anios") && configAdicional.containsKey("incluirPrevios")) {
                    Object aniosObj = configAdicional.get("anios");
                    Object incluirPreviosObj = configAdicional.get("incluirPrevios");
                    if (aniosObj instanceof Number num) {
                        aniosCredencial = num.intValue();
                    } else if (aniosObj != null) {
                        try {
                            aniosCredencial = Integer.parseInt(aniosObj.toString());
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (incluirPreviosObj instanceof Boolean bool) {
                        incluirPrevios = bool;
                    } else if (incluirPreviosObj != null) {
                        incluirPrevios = Boolean.parseBoolean(incluirPreviosObj.toString());
                    }
                }

                CotizacionCredencialesResponse cotizacionCred = generacionMovimientosService.cotizarCredenciales(
                        membresia,
                        aniosCredencial,
                        incluirPrevios,
                        desarrolloId
                );

                BigDecimal calculoTotalCred = cotizacionCred != null && cotizacionCred.calculoTotal() != null
                        ? cotizacionCred.calculoTotal()
                        : BigDecimal.ZERO;

                subtotal = calculoTotalCred.setScale(2, RoundingMode.HALF_UP);

                if (Boolean.TRUE.equals(pam.aplicaDescuento()) && porcentajeDescuentoTotal.compareTo(BigDecimal.ZERO) > 0) {
                    montoDescuento = subtotal.multiply(porcentajeDescuentoTotal)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else {
                    montoDescuento = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }

                total = subtotal.subtract(montoDescuento).setScale(2, RoundingMode.HALF_UP);
                if (cotizacionCred != null && cotizacionCred.tarifaEstablecida() != null) {
                    tarifaUnitario = cotizacionCred.tarifaEstablecida();
                }
            } else {
                // Cálculo convencional
                BigDecimal baseImporte = tarifaUnitario.multiply(BigDecimal.valueOf(cantidadMovimientos));
                if (esBaseBeneficiario) {
                    baseImporte = baseImporte.multiply(BigDecimal.valueOf(totalBeneficiariosMov));
                }
                subtotal = baseImporte.setScale(2, RoundingMode.HALF_UP);

                if (Boolean.TRUE.equals(pam.aplicaDescuento()) && porcentajeDescuentoTotal.compareTo(BigDecimal.ZERO) > 0) {
                    montoDescuento = subtotal.multiply(porcentajeDescuentoTotal)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else {
                    montoDescuento = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }

                total = subtotal.subtract(montoDescuento).setScale(2, RoundingMode.HALF_UP);
            }

            subtotalGeneral = subtotalGeneral.add(subtotal);
            descuentoGeneral = descuentoGeneral.add(montoDescuento);
            totalGeneral = totalGeneral.add(total);

            movimientosCotizados.add(CotizacionPaqueteAnualMovimientoResponse.builder()
                    .paqueteAnualMovimientoId(pam.paqueteAnualMovimientoId())
                    .movimientoId(pam.movimientoId())
                    .movimiento(pam.movimiento())
                    .cantidadMovimientos(cantidadMovimientos)
                    .baseDeCobroId(baseDeCobroId)
                    .baseDeCobro(baseDeCobro)
                    .periodicidad(periodicidadTexto)
                    .totalBeneficiarios(totalBeneficiariosMov)
                    .aplicaDescuento(pam.aplicaDescuento())
                    .obligatorio(pam.obligatorio())
                    .tarifaUnitario(tarifaUnitario)
                    .anioVigenciaCuota(anioVigenciaCuota)
                    .subtotal(subtotal)
                    .montoDescuento(montoDescuento)
                    .total(total)
                    .configuracionAdicional(configAdicional)
                    .build());
        }

        return CotizacionPaqueteAnualResponse.builder()
                .paqueteAnualId(paqueteId)
                .membresia(membresia)
                .anio(anio)
                .totalBeneficiariosActivos(totalBeneficiarios)
                .porcentajeDescuentoAplicado(porcentajeDescuentoTotal.setScale(2, RoundingMode.HALF_UP))
                .subtotalGeneral(subtotalGeneral.setScale(2, RoundingMode.HALF_UP))
                .descuentoGeneral(descuentoGeneral.setScale(2, RoundingMode.HALF_UP))
                .totalGeneral(totalGeneral.setScale(2, RoundingMode.HALF_UP))
                .esquemasAplicados(esquemasAplicados)
                .movimientos(movimientosCotizados)
                .build();
    }

    public Integer guardarPaqueteAnual(GuardarPaqueteAnualRequest request, String usuario) {
        businessLogger.info(usuario, "Solicitud de creación/actualización de paquete anual para año: {}, desarrollo: {}, tipoMembresia: {}",
                request.anio(), request.desarrollo(), request.tipoMembresia());

        String descuentosJson = null;
        if (request.configuracionDescuentos() != null) {
            try {
                descuentosJson = objectMapper.writeValueAsString(request.configuracionDescuentos());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error al serializar la configuración de descuentos a JSON", e);
            }
        }

        String movimientosJson = null;
        if (request.configuracionMovimientos() != null) {
            try {
                movimientosJson = objectMapper.writeValueAsString(request.configuracionMovimientos());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error al serializar la configuración de movimientos a JSON", e);
            }
        }

        return repository.spCobranzaGuardarPaqueteAnual(
                request.id(),
                request.anio(),
                request.tipoMembresia(),
                request.clasificacionMembresia(),
                request.desarrollo(),
                usuario,
                descuentosJson,
                movimientosJson
        ).orElseThrow(() -> new RuntimeException("Error al guardar el paquete anual en la base de datos"));
    }
}
