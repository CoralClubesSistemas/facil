package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizarPropuestaMovimientoParamDto;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizarPropuestaPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPropuestaPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.repository.PaqueteAnualRepository;
import com.coralclubes.facil.modules.sistema.service.PlantillasCuerpoCorreoService;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaqueteAnualService {

    private static final int MOVIMIENTO_CREDENCIALES = 25;
    private static final String ESQUEMA_ADICIONAL = "ADICIONAL";
    private static final String CODIGO_PLANTILLA_PROPUESTA = "PROPUESTA_PAQUETE_ANUAL";
    private static final int BASE_BENEFICIARIO = 284;

    private final PaqueteAnualRepository repository;
    private final GeneracionMovimientosService generacionMovimientosService;
    private final SociosService sociosService;
    private final PlantillasCuerpoCorreoService plantillasService;
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

    public Integer guardarPropuestaPaqueteAnual(GuardarPropuestaPaqueteAnualRequest request, String usuario) {
        businessLogger.info(usuario, "Guardando propuesta de paquete anual para membresia: {}, anio: {}, total: {}",
                request.membresia(), request.anio(), request.totalGeneral());

        String esquemasJson = null;
        if (request.esquemasAplicados() != null) {
            try {
                esquemasJson = objectMapper.writeValueAsString(request.esquemasAplicados());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error al serializar esquemas aplicados a JSON", e);
            }
        }

        String movimientosJson;
        try {
            movimientosJson = objectMapper.writeValueAsString(request.movimientos());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error al serializar movimientos a JSON", e);
        }

        String cuponesJson = null;
        if (request.cupones() != null) {
            try {
                cuponesJson = objectMapper.writeValueAsString(request.cupones());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error al serializar cupones a JSON", e);
            }
        }

        return repository.spCobranzaGuardarPropuestaPaqueteAnual(
                request.paqueteAnualId(),
                request.membresia(),
                request.anio(),
                request.totalBeneficiariosActivos(),
                request.porcentajeDescuentoAplicado(),
                request.subtotalGeneral(),
                request.descuentoGeneral(),
                request.totalGeneral(),
                esquemasJson,
                movimientosJson,
                cuponesJson,
                request.vigenciaPropuesta(),
                usuario
        ).orElseThrow(() -> new RuntimeException("Error al guardar la propuesta de paquete anual en la base de datos"));
    }

    public PropuestaPaqueteAnualResponse obtenerPropuestaPaqueteAnual(String membresia, Integer anio) {
        String jsonPropuesta = repository.spCobranzaObtenerPropuestaPaqueteAnual(membresia, anio)
                .orElse(null);

        if (jsonPropuesta == null || jsonPropuesta.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonPropuesta, PropuestaPaqueteAnualResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar la propuesta guardada de paquete anual", e);
        }
    }

    public CuerpoCorreoResponse sintetizarCuerpoCorreoPropuesta(String membresia, Integer anio) {
        PropuestaPaqueteAnualResponse propuesta = obtenerPropuestaPaqueteAnual(membresia, anio);
        if (propuesta == null) {
            throw new IllegalArgumentException("No se encontró ninguna propuesta guardada para la membresía: " + membresia + " y año: " + anio);
        }

        // 1. Obtener datos del socio
        ApiResponse<InformacionSocio> socioResponse = sociosService.obtenerSocios(membresia);
        InformacionSocio socio = socioResponse != null ? socioResponse.data() : null;

        Map<String, Object> variables = new HashMap<>();
        boolean tieneNombreSocio = socio != null && socio.nombreCompleto() != null && !socio.nombreCompleto().isBlank();
        variables.put("nombreSocio", tieneNombreSocio ? socio.nombreCompleto() : "");
        variables.put("membresia", membresia);
        variables.put("anio", anio != null ? String.valueOf(anio) : "");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        variables.put("fechaEmision", LocalDateTime.now().format(dateFormatter));

        LocalDateTime vigenciaPropuesta = propuesta.cupones().getFirst().periodoFin();
        variables.put("vigenciaPropuesta", vigenciaPropuesta != null ? vigenciaPropuesta.format(dateFormatter) : "");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        variables.put("subtotalGeneral", propuesta.subtotalGeneral() != null ? currencyFormat.format(propuesta.subtotalGeneral()) : "$0.00");
        variables.put("descuentoGeneral", propuesta.descuentoGeneral() != null ? currencyFormat.format(propuesta.descuentoGeneral()) : "$0.00");
        variables.put("totalGeneral", propuesta.totalGeneral() != null ? currencyFormat.format(propuesta.totalGeneral()) : "$0.00");
        variables.put("porcentajeDescuento", propuesta.porcentajeDescuentoAplicado() != null ? propuesta.porcentajeDescuentoAplicado().stripTrailingZeros().toPlainString() + "%" : "0%");

        // 2. Mapear movimientos
        List<Map<String, Object>> movimientosList = new ArrayList<>();
        if (propuesta.movimientos() != null) {
            for (CotizacionPaqueteAnualMovimientoResponse mov : propuesta.movimientos()) {
                Map<String, Object> movMap = new HashMap<>();
                movMap.put("movimiento", mov.movimiento());
                movMap.put("cantidadMovimientos", mov.cantidadMovimientos() != null ? mov.cantidadMovimientos() : 1);
                movMap.put("periodicidad", mov.periodicidad() != null ? mov.periodicidad() : "");
                movMap.put("totalBeneficiarios", mov.totalBeneficiarios() != null ? mov.totalBeneficiarios() : 0);
                movMap.put("tarifaUnitario", mov.tarifaUnitario() != null ? currencyFormat.format(mov.tarifaUnitario()) : "$0.00");
                movMap.put("subtotal", mov.subtotal() != null ? currencyFormat.format(mov.subtotal()) : "$0.00");
                movMap.put("montoDescuento", mov.montoDescuento() != null ? currencyFormat.format(mov.montoDescuento()) : "$0.00");
                movMap.put("total", mov.total() != null ? currencyFormat.format(mov.total()) : "$0.00");
                movMap.put("aplicaDescuento", Boolean.TRUE.equals(mov.aplicaDescuento()));
                movimientosList.add(movMap);
            }
        }
        variables.put("movimientos", movimientosList);

        // 3. Mapear esquemas aplicados
        List<Map<String, Object>> esquemasList = new ArrayList<>();
        if (propuesta.esquemasAplicados() != null) {
            for (PaqueteAnualDescuentoResponse esq : propuesta.esquemasAplicados()) {
                Map<String, Object> esqMap = new HashMap<>();
                esqMap.put("label", esq.label() != null ? esq.label() : esq.value());
                esqMap.put("descuento", esq.descuento() != null ? esq.descuento().stripTrailingZeros().toPlainString() + "%" : "0%");
                esquemasList.add(esqMap);
            }
        }
        variables.put("esquemasAplicados", esquemasList);

        // 4. Mapear cupones de beneficio
        List<Map<String, Object>> cuponesList = new ArrayList<>();
        if (propuesta.cupones() != null) {
            for (CuponBeneficioPaqueteAnualResponse cup : propuesta.cupones()) {
                Map<String, Object> cupMap = new HashMap<>();
                cupMap.put("cupon", cup.cupon());
                cupMap.put("cantidadCupones", cup.cantidadCupones() != null ? cup.cantidadCupones() : 1);
                cuponesList.add(cupMap);
            }
        }
        variables.put("cupones", cuponesList);

        // 5. Renderizar plantilla con Pebble
        String asunto = plantillasService.renderizarAsunto(CODIGO_PLANTILLA_PROPUESTA, variables);
        String cuerpo = plantillasService.renderizarCuerpo(CODIGO_PLANTILLA_PROPUESTA, variables);

        return CuerpoCorreoResponse.builder()
                .asunto(asunto)
                .cuerpo(cuerpo)
                .build();
    }
}
