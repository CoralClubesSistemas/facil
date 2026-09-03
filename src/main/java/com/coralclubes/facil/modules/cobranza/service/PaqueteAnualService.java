package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizacionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizarPropuestaMovimientoParamDto;
import com.coralclubes.facil.modules.cobranza.dto.request.CotizarPropuestaPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPropuestaPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.VenderPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.model.generador_movimientos.MovimientoPaqueteAnualStrategy;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaqueteAnualService {

    private static final int MOVIMIENTO_CREDENCIALES = 25;
    private static final String ESQUEMA_ADICIONAL = "ADICIONAL";
    private static final String CODIGO_PLANTILLA_PROPUESTA = "PROPUESTA_PAQUETE_ANUAL";

    private final PaqueteAnualRepository repository;
    private final GeneracionMovimientosService generacionMovimientosService;
    private final CobranzaService cobranzaService;
    private final IntentoPagoService intentoPagoService;
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

        // 4. Consultar total de beneficiarios computables (activos o bloqueados)
        Integer totalBeneficiarios = repository.spCobranzaObtenerBeneficiariosPaqueteAnual(membresia);

        // 5. Determinar esquemas aplicados y sumar el porcentaje total de descuento
        List<PaqueteAnualDescuentoResponse> esquemasConfigurados = paqueteDetalle.configuracionDescuentos() != null
                ? paqueteDetalle.configuracionDescuentos()
                : Collections.emptyList();

        List<PaqueteAnualDescuentoResponse> esquemasAplicados = new ArrayList<>();
        BigDecimal porcentajeDescuentoTotal = BigDecimal.ZERO;

        for (String esquemaKey : esquemasSeleccionados) {
            esquemasConfigurados.stream()
                    .filter(d -> d.value() != null && d.value().equalsIgnoreCase(esquemaKey))
                    .findFirst()
                    .ifPresent(esquemasAplicados::add);
        }

        for (PaqueteAnualDescuentoResponse d : esquemasAplicados) {
            if (d.descuento() != null) {
                porcentajeDescuentoTotal = porcentajeDescuentoTotal.add(d.descuento());
            }
        }

        // 6. Procesar y cotizar cada movimiento mediante la capa de generación de movimientos
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

            Map<String, Object> configAdicional = new HashMap<>();
            if (pam.configuracionAdicional() != null) {
                configAdicional.putAll(pam.configuracionAdicional());
            }
            if (paramsPorMovimiento.containsKey(movId)) {
                configAdicional.putAll(paramsPorMovimiento.get(movId));
            }
            if (pam.cuotaVigente() != null && !configAdicional.containsKey("cuota")) {
                configAdicional.put("cuota", pam.cuotaVigente());
            }
            if (!configAdicional.containsKey("anioCompleto")) {
                configAdicional.put("anioCompleto", anio);
            }

            CotizacionMovimientoRequest cotReq = CotizacionMovimientoRequest.builder()
                    .membresia(membresia)
                    .tipoMovimientoId(movId)
                    .desarrolloConsumo(desarrolloId)
                    .parametrosEspeciales(configAdicional)
                    .build();

            CotizacionMovimientoResponse cotizacionMov = generacionMovimientosService.cotizarMovimiento(cotReq);

            BigDecimal subtotal = cotizacionMov.subtotal() != null
                    ? cotizacionMov.subtotal().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            BigDecimal montoDescuento;
            if (Boolean.TRUE.equals(pam.aplicaDescuento()) && porcentajeDescuentoTotal.compareTo(BigDecimal.ZERO) > 0) {
                montoDescuento = subtotal.multiply(porcentajeDescuentoTotal)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                montoDescuento = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal total = subtotal.subtract(montoDescuento).setScale(2, RoundingMode.HALF_UP);

            subtotalGeneral = subtotalGeneral.add(subtotal);
            descuentoGeneral = descuentoGeneral.add(montoDescuento);
            totalGeneral = totalGeneral.add(total);

            movimientosCotizados.add(CotizacionPaqueteAnualMovimientoResponse.builder()
                    .paqueteAnualMovimientoId(pam.paqueteAnualMovimientoId())
                    .movimientoId(pam.movimientoId())
                    .movimiento(pam.movimiento())
                    .cantidadMovimientos(cotizacionMov.cantidadMovimientos())
                    .baseDeCobroId(cotizacionMov.baseDeCobroId())
                    .baseDeCobro(cotizacionMov.baseDeCobro())
                    .periodicidad(cotizacionMov.periodicidad())
                    .totalBeneficiarios(cotizacionMov.totalBeneficiarios())
                    .aplicaDescuento(pam.aplicaDescuento())
                    .obligatorio(pam.obligatorio())
                    .tarifaUnitario(cotizacionMov.tarifaUnitario())
                    .anioVigenciaCuota(cotizacionMov.anioVigenciaCuota())
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

        // definimos la fecha de vigencia de la propuesta como el ultimo dia del periodo valido de cupones
        LocalDateTime fechaVigencia = request.cupones() != null && !request.cupones().isEmpty()
                ? request.cupones().getFirst().periodoFin()
                : null;

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
                fechaVigencia,
                request.fechaPrevistaCompra(),
                usuario
        ).orElseThrow(() -> new RuntimeException("Error al guardar la propuesta de paquete anual en la base de datos"));
    }

    public PropuestaPaqueteAnualResponse obtenerPropuestaPaqueteAnual(String membresia, Integer anio, Integer propuestaId) {
        String jsonPropuesta = repository.spCobranzaObtenerPropuestaPaqueteAnual(membresia, anio, propuestaId)
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

    public PropuestaPaqueteAnualResponse obtenerPropuestaPaqueteAnual(String membresia, Integer anio) {
        return obtenerPropuestaPaqueteAnual(membresia, anio, null);
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

        LocalDateTime vigenciaPropuesta = propuesta.vigenciaPropuesta();
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

    public VenderPaqueteAnualResponse venderPaqueteAnual(VenderPaqueteAnualRequest request, String usuario) {
        String membresia = request.membresia();
        Integer propuestaId = request.propuestaId();

        businessLogger.info(usuario, "Iniciando venta de paquete anual para membresía: {}, propuestaId: {}", membresia, propuestaId);

        // 1. Extraer la propuesta activa (por propuestaId o membresía/año)
        PropuestaPaqueteAnualResponse propuesta = obtenerPropuestaPaqueteAnual(membresia, request.anio(), propuestaId);

        // 2. Validaciones de seguridad y negocio
        validarPropuestaParaVenta(propuesta, request);

        Integer anio = propuesta.anio();

        // 3. Consultar datos del socio y desarrollo
        ApiResponse<InformacionSocio> socioResponse = sociosService.obtenerSocios(membresia);
        InformacionSocio socio = socioResponse != null ? socioResponse.data() : null;
        Integer desarrolloId = socio != null ? socio.desarrolloId() : null;

        LocalDate fechaVencimiento = propuesta.vigenciaPropuesta() != null
                ? propuesta.vigenciaPropuesta().toLocalDate()
                : LocalDate.now();

        List<MovimientoGeneradoPaqueteAnualDto> movimientosGeneradosDto = new ArrayList<>();
        List<GenerarOrdenCobranzaMovimientoRequest> movimientosParaOrden = new ArrayList<>();

        // 4. Generar movimientos originales de la propuesta
        generarMovimientosOriginales(propuesta, desarrolloId, fechaVencimiento, usuario, movimientosGeneradosDto, movimientosParaOrden);

        // 5. Generar movimiento adicional de 'PAQUETE ANUAL' ($1.00)
        MovimientoManualResponse movPaqueteAnual = generarMovimientoPaqueteAnualUnPeso(propuesta, request, desarrolloId, usuario);

        // 6. Aplicar ajuste compensatorio de $1.00 para cuadrar el total con la propuesta
        aplicarAjusteCompensatorioUnPeso(movimientosGeneradosDto, movimientosParaOrden, movPaqueteAnual, anio, usuario);

        if (movimientosParaOrden.isEmpty()) {
            throw new IllegalStateException("No se pudieron generar movimientos para crear la orden de cobranza.");
        }

        // 7. Generar orden de cobranza usando CobranzaService con redacción profesional
        String mensajeAdicional = request.mensajeAdicional() != null && !request.mensajeAdicional().isBlank()
                ? request.mensajeAdicional()
                : "Esta orden de cobranza procede de una generación automática de Paquete Anual. No eliminar a menos que se requiera aplicar las configuraciones de forma manual para liquidar el paquete.";

        GenerarOrdenCobranzaRequest ordenRequest = GenerarOrdenCobranzaRequest.builder()
                .membresia(membresia)
                .movimientos(movimientosParaOrden)
                .agregarIva(false)
                .ivaIncluido(false)
                .mensajeAdicional(mensajeAdicional)
                .build();

        ApiResponse<GenerarOrdenCobranzaResponse> ordenResponse = cobranzaService.generarOrdenCobranza(ordenRequest, usuario);
        GenerarOrdenCobranzaResponse ordenData = ordenResponse != null ? ordenResponse.data() : null;

        if (ordenData == null) {
            throw new IllegalStateException("No se pudo generar la orden de cobranza para la venta de paquete anual.");
        }

        businessLogger.info(usuario, "Orden de cobranza {} ({}) generada exitosamente para venta de paquete anual membresía: {}",
                ordenData.numeroOrden(),
                ordenData.ordenUuid(),
                membresia);

        // 8. Generar intento de pago con Link de Pago si la bandera viene activa
        String linkPagoCheckoutUrl = null;
        Integer intentoPagoId = null;

        if (Boolean.TRUE.equals(request.generarOrdenLinkPago())) {
            businessLogger.info(usuario, "Generando link de pago para la orden {} de paquete anual membresía {}",
                    ordenData.numeroOrden(), membresia);

            String correoLinkPago = request.correoEnvioLinkPago() != null && !request.correoEnvioLinkPago().isBlank()
                    ? request.correoEnvioLinkPago().trim()
                    : (socio != null && socio.correo() != null ? socio.correo().trim() : "");

            ProcesarPagoRequest pagoRequest = ProcesarPagoRequest.builder()
                    .formaPagoClave("LINK")
                    .monto(propuesta.totalGeneral())
                    .metadata(Map.of("correoUsuario", correoLinkPago))
                    .build();

            ProcesarPagoResponse pagoResponse = intentoPagoService.iniciarPago(ordenData.ordenUuid(), pagoRequest, usuario);

            if (pagoResponse != null) {
                intentoPagoId = pagoResponse.intentoPagoId();
                if (pagoResponse.datosAdicionales() != null && pagoResponse.datosAdicionales().containsKey("checkoutUrl")) {
                    linkPagoCheckoutUrl = String.valueOf(pagoResponse.datosAdicionales().get("checkoutUrl"));
                }
            }
        }

        // 9. Retornar respuesta consolidada
        return VenderPaqueteAnualResponse.builder()
                .propuestaId(propuesta.propuestaId())
                .paqueteAnualId(propuesta.paqueteAnualId())
                .membresia(membresia)
                .anio(anio)
                .numeroOrden(ordenData.numeroOrden())
                .ordenUuid(ordenData.ordenUuid())
                .desarrolloId(ordenData.desarrolloId() != null ? ordenData.desarrolloId() : desarrolloId)
                .subtotalGeneral(propuesta.subtotalGeneral())
                .descuentoGeneral(propuesta.descuentoGeneral())
                .totalGeneral(propuesta.totalGeneral())
                .porcentajeDescuentoAplicado(propuesta.porcentajeDescuentoAplicado())
                .movimientosGenerados(movimientosGeneradosDto)
                .esquemasAplicados(propuesta.esquemasAplicados())
                .cupones(propuesta.cupones())
                .linkPagoCheckoutUrl(linkPagoCheckoutUrl)
                .intentoPagoId(intentoPagoId)
                .build();
    }

    private void validarPropuestaParaVenta(PropuestaPaqueteAnualResponse propuesta, VenderPaqueteAnualRequest request) {
        if (propuesta == null) {
            throw new IllegalArgumentException("No se encontró ninguna propuesta activa de paquete anual para la membresía: "
                    + request.membresia() + (request.propuestaId() != null ? " y propuestaId: " + request.propuestaId() : ""));
        }

        if (request.membresia() != null && !request.membresia().trim().equalsIgnoreCase(propuesta.membresia().trim())) {
            throw new IllegalArgumentException("La membresía proporcionada (" + request.membresia()
                    + ") no coincide con la membresía de la propuesta (" + propuesta.membresia() + ")");
        }

        if (propuesta.movimientos() == null || propuesta.movimientos().isEmpty()) {
            throw new IllegalStateException("La propuesta de paquete anual no contiene movimientos configurados.");
        }

        if (propuesta.vigenciaPropuesta() != null && LocalDateTime.now().isAfter(propuesta.vigenciaPropuesta())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            throw new IllegalStateException("La propuesta de paquete anual ha expirado con fecha límite: "
                    + propuesta.vigenciaPropuesta().format(formatter));
        }
    }

    private void generarMovimientosOriginales(
            PropuestaPaqueteAnualResponse propuesta,
            Integer desarrolloId,
            LocalDate fechaVencimiento,
            String usuario,
            List<MovimientoGeneradoPaqueteAnualDto> movimientosGeneradosDto,
            List<GenerarOrdenCobranzaMovimientoRequest> movimientosParaOrden
    ) {
        Integer anio = propuesta.anio();
        String membresia = propuesta.membresia();

        for (CotizacionPaqueteAnualMovimientoResponse mov : propuesta.movimientos()) {
            Map<String, Object> paramsEspeciales = new HashMap<>();

            if (mov.configuracionAdicional() != null) {
                paramsEspeciales.putAll(mov.configuracionAdicional());
            }

            if (mov.movimientoId() != null && mov.movimientoId() == MOVIMIENTO_CREDENCIALES) {
                if (!paramsEspeciales.containsKey("anios")) {
                    paramsEspeciales.put("anios", 1);
                }
                if (paramsEspeciales.containsKey("incluirPrevios") && !paramsEspeciales.containsKey("incluyePrevios")) {
                    paramsEspeciales.put("incluyePrevios", paramsEspeciales.get("incluirPrevios"));
                }
            } else {
                paramsEspeciales.put("cantidadMovimientos", mov.cantidadMovimientos() != null ? mov.cantidadMovimientos() : 1);
                paramsEspeciales.put("descripcion", mov.movimiento() != null ? mov.movimiento() : "");
                paramsEspeciales.put("cuota", mov.tarifaUnitario() != null ? mov.tarifaUnitario() : BigDecimal.ZERO);
                if (!paramsEspeciales.containsKey("anioCompleto")) {
                    paramsEspeciales.put("anioCompleto", anio);
                }
            }

            GeneracionMovimientoRequest genReq = GeneracionMovimientoRequest.builder()
                    .membresia(membresia)
                    .tipoMovimientoId(mov.movimientoId())
                    .fechaVencimiento(fechaVencimiento)
                    .desarrolloConsumo(desarrolloId)
                    .parametrosEspeciales(paramsEspeciales)
                    .build();

            List<MovimientoManualResponse> movimientosInsertados = generacionMovimientosService.generarMovimiento(genReq, usuario);

            if (movimientosInsertados == null || movimientosInsertados.isEmpty()) {
                businessLogger.warn(usuario, "No se retornaron registros insertados para el tipo de movimiento: {}", mov.movimientoId());
                continue;
            }

            BigDecimal totalDescuentoConcepto = mov.montoDescuento() != null ? mov.montoDescuento() : BigDecimal.ZERO;
            int totalGenerados = movimientosInsertados.size();
            BigDecimal descuentoUnitarioBase = totalDescuentoConcepto.divide(BigDecimal.valueOf(totalGenerados), 2, RoundingMode.HALF_UP);
            BigDecimal descuentoAcumulado = BigDecimal.ZERO;

            for (int i = 0; i < totalGenerados; i++) {
                MovimientoManualResponse m = movimientosInsertados.get(i);

                BigDecimal descuentoMov;
                if (i == totalGenerados - 1) {
                    descuentoMov = totalDescuentoConcepto.subtract(descuentoAcumulado).setScale(2, RoundingMode.HALF_UP);
                } else {
                    descuentoMov = descuentoUnitarioBase;
                    descuentoAcumulado = descuentoAcumulado.add(descuentoMov);
                }

                BigDecimal cuotaUnitario = m.cuota() != null ? m.cuota() : BigDecimal.ZERO;
                BigDecimal totalMov = cuotaUnitario.subtract(descuentoMov).setScale(2, RoundingMode.HALF_UP);

                movimientosGeneradosDto.add(MovimientoGeneradoPaqueteAnualDto.builder()
                        .mvtId(m.mvtId())
                        .tipoMovimientoId(mov.movimientoId())
                        .descripcion(m.descripcion())
                        .cuota(cuotaUnitario)
                        .montoDescuento(descuentoMov)
                        .total(totalMov)
                        .fechaVencimiento(m.fechaVencimiento())
                        .build());

                String justificacionDescuento = descuentoMov.compareTo(BigDecimal.ZERO) > 0
                        ? ("Descuento Paquete Anual " + anio + (propuesta.porcentajeDescuentoAplicado() != null ? " (" + propuesta.porcentajeDescuentoAplicado().stripTrailingZeros().toPlainString() + "%)" : ""))
                        : null;

                movimientosParaOrden.add(GenerarOrdenCobranzaMovimientoRequest.builder()
                        .idMovimiento(m.mvtId())
                        .montoCapital(cuotaUnitario)
                        .montoInteres(BigDecimal.ZERO)
                        .interesPago(BigDecimal.ZERO)
                        .interesesBonificados(BigDecimal.ZERO)
                        .totalDescuento(descuentoMov)
                        .justificacionDescuento(justificacionDescuento)
                        .usuarioAutoriza(usuario)
                        .build());
            }
        }
    }

    private MovimientoManualResponse generarMovimientoPaqueteAnualUnPeso(
            PropuestaPaqueteAnualResponse propuesta,
            VenderPaqueteAnualRequest request,
            Integer desarrolloId,
            String usuario
    ) {
        Map<String, Object> paramsPaquete = new HashMap<>();
        paramsPaquete.put("anio", propuesta.anio());
        paramsPaquete.put("procedenciaId", request.procedenciaId());
        paramsPaquete.put("procedenciaLabel", request.procedencia());
        paramsPaquete.put("cuota", BigDecimal.ONE);

        GeneracionMovimientoRequest genReq = GeneracionMovimientoRequest.builder()
                .membresia(propuesta.membresia())
                .tipoMovimientoId(MovimientoPaqueteAnualStrategy.TIPO_MOVIMIENTO_PAQUETE_ANUAL)
                .fechaVencimiento(LocalDate.of(propuesta.anio(), 1, 1))
                .desarrolloConsumo(desarrolloId)
                .parametrosEspeciales(paramsPaquete)
                .build();

        List<MovimientoManualResponse> insertados = generacionMovimientosService.generarMovimiento(genReq, usuario);
        if (insertados == null || insertados.isEmpty()) {
            throw new IllegalStateException("No se pudo generar el movimiento de Paquete Anual de $1.00 para la membresía: " + propuesta.membresia());
        }
        return insertados.getFirst();
    }

    private void aplicarAjusteCompensatorioUnPeso(
            List<MovimientoGeneradoPaqueteAnualDto> movimientosGeneradosDto,
            List<GenerarOrdenCobranzaMovimientoRequest> movimientosParaOrden,
            MovimientoManualResponse movPaqueteAnual,
            Integer anio,
            String usuario
    ) {
        BigDecimal unPeso = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);

        // 1. Localizar un movimiento original elegible donde el total a pagar sea >= $1.00
        int indiceAjustar = -1;
        for (int i = 0; i < movimientosParaOrden.size(); i++) {
            GenerarOrdenCobranzaMovimientoRequest movOrden = movimientosParaOrden.get(i);
            BigDecimal montoNeto = movOrden.montoCapital().subtract(
                    movOrden.totalDescuento() != null ? movOrden.totalDescuento() : BigDecimal.ZERO
            );
            if (montoNeto.compareTo(unPeso) >= 0) {
                indiceAjustar = i;
                break;
            }
        }

        if (indiceAjustar == -1 && !movimientosParaOrden.isEmpty()) {
            indiceAjustar = 0;
        }

        if (indiceAjustar != -1) {
            GenerarOrdenCobranzaMovimientoRequest movOriginalOrden = movimientosParaOrden.get(indiceAjustar);
            BigDecimal nuevoDescuento = (movOriginalOrden.totalDescuento() != null ? movOriginalOrden.totalDescuento() : BigDecimal.ZERO)
                    .add(unPeso).setScale(2, RoundingMode.HALF_UP);

            String justificacion = movOriginalOrden.justificacionDescuento() != null
                    ? movOriginalOrden.justificacionDescuento() + " (Ajuste $1.00 Paquete Anual)"
                    : "Descuento Paquete Anual " + anio + " (Ajuste $1.00 Paquete Anual)";

            movimientosParaOrden.set(indiceAjustar, GenerarOrdenCobranzaMovimientoRequest.builder()
                    .idMovimiento(movOriginalOrden.idMovimiento())
                    .montoCapital(movOriginalOrden.montoCapital())
                    .montoInteres(movOriginalOrden.montoInteres())
                    .interesPago(movOriginalOrden.interesPago())
                    .interesesBonificados(movOriginalOrden.interesesBonificados())
                    .totalDescuento(nuevoDescuento)
                    .justificacionDescuento(justificacion)
                    .usuarioAutoriza(usuario)
                    .build());

            // Ajustar también en movimientosGeneradosDto
            for (int j = 0; j < movimientosGeneradosDto.size(); j++) {
                MovimientoGeneradoPaqueteAnualDto dto = movimientosGeneradosDto.get(j);
                if (dto.mvtId() != null && dto.mvtId().equals(movOriginalOrden.idMovimiento())) {
                    BigDecimal nuevoTotal = dto.total().subtract(unPeso).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal dtoDescuento = dto.montoDescuento() != null ? dto.montoDescuento().add(unPeso) : unPeso;

                    movimientosGeneradosDto.set(j, MovimientoGeneradoPaqueteAnualDto.builder()
                            .mvtId(dto.mvtId())
                            .tipoMovimientoId(dto.tipoMovimientoId())
                            .descripcion(dto.descripcion())
                            .cuota(dto.cuota())
                            .montoDescuento(dtoDescuento)
                            .total(nuevoTotal)
                            .fechaVencimiento(dto.fechaVencimiento())
                            .build());
                    break;
                }
            }
        }

        // 2. Agregar el movimiento de Paquete Anual ($1.00) tanto a la orden como a los generados
        movimientosParaOrden.add(GenerarOrdenCobranzaMovimientoRequest.builder()
                .idMovimiento(movPaqueteAnual.mvtId())
                .montoCapital(unPeso)
                .montoInteres(BigDecimal.ZERO)
                .interesPago(BigDecimal.ZERO)
                .interesesBonificados(BigDecimal.ZERO)
                .totalDescuento(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .justificacionDescuento(null)
                .usuarioAutoriza(usuario)
                .build());

        movimientosGeneradosDto.add(MovimientoGeneradoPaqueteAnualDto.builder()
                .mvtId(movPaqueteAnual.mvtId())
                .tipoMovimientoId(MovimientoPaqueteAnualStrategy.TIPO_MOVIMIENTO_PAQUETE_ANUAL)
                .descripcion(movPaqueteAnual.descripcion())
                .cuota(unPeso)
                .montoDescuento(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .total(unPeso)
                .fechaVencimiento(movPaqueteAnual.fechaVencimiento())
                .build());
    }
}
