package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleDto;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosMembresia;
import com.coralclubes.facil.modules.clientes.service.PuntosService;
import com.coralclubes.facil.shared.events.dto.ConsumoPuntosReservacionEvent;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.ConfirmacionReservaResponse;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.modules.reservaciones.dto.projection.DisponibilidadUnidadProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.engine.PromocionesEngine;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import com.coralclubes.facil.shared.domain.dto.PaginaResponse;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservacionesService {

    private final ReservacionesRepository repository;
    private final StorageClient storageClient;
    private final PromocionesService promocionesService;
    private final PromocionesEngine promocionesEngine;
    private final CampanasPuntosService campanasPuntosService;
    private final UserContext userContext;
    private final PuntosService puntosService;
    private final BusinessLogger businessLogger;

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationClient notificationClient;
    private final AmaDeLlavesService amaDeLlavesService;

    private final CobranzaService cobranzaService;

    @Value("${app.clients.notifications.aliases.default}")
    private String aliasConfigNotificaciones;

    @Value("${app.clients.notifications.templates.reserva-cancelada}")
    private String templateReservaCancelada;

    // =========================================================================
    // 1. GESTIÓN DE INVENTARIO Y DISPONIBILIDAD
    // =========================================================================

    public ApiResponse<List<DisponibilidadUnidadDto>> buscarDisponibilidad(BusquedaDisponibilidadRequest request) {
        validarFechas(request.fechaEntrada(), request.fechaSalida());

        List<DisponibilidadUnidadProjection> resultados = repository.buscarDisponibilidad(
                request.destinoId(), request.fechaEntrada(), request.fechaSalida(), request.personas(), request.membresia()
        );

        List<DisponibilidadUnidadDto> disponibilidadDtos = resultados.stream().map(projection -> {
            String imagenUrl = projection.uuidImagen() != null ? storageClient.obtenerUrlDescarga(projection.uuidImagen()) : null;
            return DisponibilidadUnidadDto.builder()
                    .idTipoUnidad(projection.idTipoUnidad())
                    .nombreUnidad(projection.nombreUnidad())
                    .descripcionCorta(projection.descripcionCorta())
                    .capacidad(projection.capacidad())
                    .stockDisponible(projection.stockDisponible())
                    .costoEstancia(projection.costoEstancia())
                    .urlImagen(imagenUrl)
                    .build();
        }).toList();

        return ApiResponse.success("Disponibilidad calculada exitosamente", disponibilidadDtos);
    }

    public ApiResponse<UUID> bloquearInventarioTemporal(CrearReservaTemporalRequest request, String ipAddress) {
        validarFechas(request.fechaEntrada(), request.fechaSalida());

        String jsonCarrito = JsonUtils.toJson(request.carrito());

        UUID groupId = repository.spResvCrearReservaTemporal(
                jsonCarrito, request.fechaEntrada(), request.fechaSalida(), request.membresia(), ipAddress
        );
        return ApiResponse.success("Inventario bloqueado exitosamente.", groupId);
    }

    public ApiResponse<Boolean> liberarInventario(UUID groupId) {
        repository.eliminarReservaTemporal(groupId);
        return ApiResponse.success("Inventario liberado exitosamente.", true);
    }

    // =========================================================================
    // 2. CHECKOUT Y CÁLCULOS FINANCIEROS (BFF)
    // =========================================================================

    public ApiResponse<List<CuponDisponibleDto>> obtenerCuponesDisponibles(UUID groupId) {
        return ApiResponse.success("Cupones obtenidos", repository.obtenerCuponesCarrito(groupId));
    }

    public ApiResponse<ResumenCheckoutResponse> calcularCheckout(CalcularCheckoutRequest request) {
        // 1. Obtener Desglose Base
        String jsonDesglose = repository.obtenerDesgloseFinancieroJson(request.groupId());
        if (jsonDesglose == null) throw new IllegalArgumentException("El carrito expiró o no existe.");

        List<ResumenCheckoutResponse.ItemCheckoutDto> habitaciones = JsonUtils.fromJson(jsonDesglose, new TypeReference<>() {
        });

        ReservacionContexto contexto = obtenerContextoHidratado(request.groupId());
        PuntosMembresia puntosMembresia = puntosService.obtenerPuntosMembresia(contexto.getMembresia());

        // 2. Obtener opciones de Puntos desde la BD
        List<OpcionPagoPuntosDto> opcionesPuntos = campanasPuntosService.evaluarPromocionesCarrito(request.groupId());

        // 3. Procesar las opciones de Puntos habitación por habitación
        for (var hab : habitaciones) {
            // A. Le pegamos su Card de Puntos (si es que SQL Server devolvió una para este rrtId)
            opcionesPuntos.stream()
                    .filter(op -> op.rrtId().equals(hab.getRrtId()))
                    .filter(op -> op.costoTotalPuntos() <= (puntosMembresia != null ? puntosMembresia.saldoPuntosNeto() : 0)) // Solo asignamos la opción si el usuario tiene puntos suficientes
                    .findFirst()
                    .ifPresent(hab::setOpcionPagoPuntos);

            // B. Si Angular nos dice que el usuario hizo clic en "Pagar con Puntos" para esta unidad
            if (request.rrtIdsPagoPuntos() != null && request.rrtIdsPagoPuntos().contains(hab.getRrtId())) {

                // Solo aplicamos si la BD confirmó que sí es elegible
                if (hab.getOpcionPagoPuntos() != null) {
                    hab.setDescuentoAplicado(hab.getSubtotalHabitacion()); // 100% DE DESCUENTO
                    hab.setMotivoDescuento("Pago con Puntos (" + hab.getOpcionPagoPuntos().costoTotalPuntos() + " pts)");
                }
            }
        }

        // 4. Evaluar Beneficios Tradicionales (Cupones / Promo de Buen Fin)
        // Solo para las habitaciones que NO se van a pagar con puntos
        ResultadoBeneficio beneficio = new ResultadoBeneficio(BigDecimal.ZERO, false, null, null, null);

        if (request.cupon() != null || (request.codigoPromocion() != null && !request.codigoPromocion().isBlank())) {
            // Evaluamos y aplicamos (Tu método interno actual)
            beneficio = evaluarBeneficios(request.cupon(), request.codigoPromocion(), contexto, habitaciones);
        }

        // 5. Consolidar Totales Finales por Habitación
        habitaciones.forEach(hab -> {
            if (hab.getDescuentoAplicado() == null) hab.setDescuentoAplicado(BigDecimal.ZERO);
            hab.setTotalFinalHabitacion(hab.getSubtotalHabitacion().subtract(hab.getDescuentoAplicado()));
        });

        // 6. Cálculos Generales e Impuestos (Solo sobre lo que quedó en MXN)
        BigDecimal subtotalOriginal = habitaciones.stream().map(ResumenCheckoutResponse.ItemCheckoutDto::getSubtotalHabitacion).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAPagar = habitaciones.stream().map(ResumenCheckoutResponse.ItemCheckoutDto::getTotalFinalHabitacion).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Ensamblaje
        var resumen = ResumenCheckoutResponse.ResumenFinancieroDto.builder()
                .subtotalOriginal(subtotalOriginal)
                .totalDescuentos(subtotalOriginal.subtract(totalAPagar))
                .totalAPagar(totalAPagar)
                .cuponValido(beneficio.esValido())
                .mensajeCupon(beneficio.mensaje())
                .build();

        return ApiResponse.success("Desglose calculado correctamente", ResumenCheckoutResponse.builder().habitaciones(habitaciones).resumen(resumen).build());
    }

    // =========================================================================
    // 3. CONFIRMACIÓN FINAL DE RESERVACIÓN
    // =========================================================================

    @Transactional
    public ApiResponse<ConfirmacionReservaResponse> confirmarReservacionConOrden(ConfirmarReservaRequest request) {
        List<Integer> foliosGenerados = confirmarReservacion(request).data();

        // consultamos el listado de movimientos generados para estos folios
        List<GenerarOrdenCobranzaMovimientoRequest> movimientos = foliosGenerados.stream()
                .flatMap(folio -> repository.obtenerCargosReservacion(request.membresia(), folio).stream()
                        .map(cargo -> {
                            return GenerarOrdenCobranzaMovimientoRequest.builder()
                                    .idMovimiento(cargo.idMovimiento())
                                    .montoCapital(cargo.importePendiente())
                                    .montoInteres(BigDecimal.ZERO) // Por ahora no manejamos intereses en la reserva, solo el capital
                                    .interesPago(BigDecimal.ZERO) // El cliente no paga intereses, solo el capital
                                    .interesesBonificados(BigDecimal.ZERO)
                                    .totalDescuento(BigDecimal.ZERO)
                                    .justificacionDescuento(null)
                                    .usuarioAutoriza(userContext.getUsername())
                                    .build();
                        }))
                .toList();

        // construimos la request de creacion de orden
        var ordenRequest = GenerarOrdenCobranzaRequest.builder()
                .membresia(request.membresia())
                .movimientos(movimientos)
                .agregarIva(false)
                .ivaIncluido(false)
                .build();

        UUID uuidOrden = cobranzaService.generarOrdenCobranza(ordenRequest, userContext.getUsername()).data().ordenUuid();

        return ApiResponse.success(new ConfirmacionReservaResponse(foliosGenerados, uuidOrden));
    }

    @Transactional
    public ApiResponse<List<Integer>> confirmarReservacion(ConfirmarReservaRequest request) {
        String usuario = userContext.getUsername();
        ReservacionContexto contexto = obtenerContextoHidratado(request.groupId());

        validarOcupantesVsHabitaciones(request.totalPersonas(), contexto.getItems().size());

        // 1. Evaluar si hay beneficios tradicionales (Solo si no hay pagos con puntos)
        ResultadoBeneficio beneficio = new ResultadoBeneficio(BigDecimal.ZERO, false, null, null, null);
        if (request.rrtIdsPagoPuntos() == null || request.rrtIdsPagoPuntos().isEmpty()) {
            beneficio = evaluarBeneficiosSobreContexto(request.cupon(), request.codigoPromocion(), contexto);
        }

        // 2. Obtener el tabulador de puntos (para saber cuántos puntos cuesta cada cuarto)
        List<OpcionPagoPuntosDto> opcionesPuntos = campanasPuntosService.evaluarPromocionesCarrito(request.groupId());

        // 3. Armar el JSON
        List<DetalleReservacionJson> listaDetalles = generarListaDetallesParaBaseDatos(
                contexto,
                request.totalPersonas(),
                beneficio.montoDescuento(),
                request.rrtIdsPagoPuntos()
        );

        // 4. LA LLAMADA AL CONTADOR
        String jsonPayload = JsonUtils.toJson(listaDetalles);
        List<Integer> consecutivosGenerados = repository.guardarReservacionFisica(request, usuario, jsonPayload);

        if (consecutivosGenerados == null || consecutivosGenerados.isEmpty()) {
            throw new RuntimeException("Error crítico: La base de datos no devolvió folios de reservación.");
        }

        Integer folioPrincipal = consecutivosGenerados.getFirst();

        // Quemar beneficios o puntos según corresponda
        if (beneficio.esValido() && !beneficio.montoDescuento().equals(BigDecimal.ZERO)) {
            // Quema Cupones o Promociones normales
            quemarBeneficiosEnBaseDeDatos(
                    beneficio.tipoAplicado(),
                    contexto.getMembresia(),
                    folioPrincipal,
                    request.codigoPromocion(),
                    request.cupon(),
                    usuario
            );
        }

        // ====================================================================
        // 6. PUBLICAR EVENTO DE CONSUMO DE PUNTOS
        // ====================================================================
        if (request.rrtIdsPagoPuntos() != null && !request.rrtIdsPagoPuntos().isEmpty()) {

            // Iteramos solo las habitaciones que se seleccionaron para pagar con puntos
            for (OpcionPagoPuntosDto opcion : opcionesPuntos) {
                if (request.rrtIdsPagoPuntos().contains(opcion.rrtId())) {

                    // Buscamos a qué desarrollo (hotel) pertenece esta habitación desde el contexto
                    Integer desarrolloId = contexto.getIdDesarrollo();

                    if (desarrolloId != null) {
                        ConsumoPuntosReservacionEvent consumoPuntosEvent = ConsumoPuntosReservacionEvent.builder()
                                .membresia(contexto.getMembresia())
                                .desarrolloId(desarrolloId)
                                .totalPuntos(opcion.costoTotalPuntos())
                                .idMovimiento(folioPrincipal)
                                .descripcion("RESERVA CON PUNTOS - " + opcion.nombrePromocion())
                                .usuario(usuario)
                                .build();

                        // Publicamos el evento
                        eventPublisher.publishEvent(consumoPuntosEvent);
                    }
                }
            }
        }

        // ====================================================================
        // 7. PUBLICAR EVENTO DE RESERVACIÓN CONFIRMADA (POST-PROCESAMIENTO ASÍNCRONO)
        // ====================================================================
        try {
            BigDecimal subtotal = listaDetalles.stream()
                    .map(d -> d.importeOriginal().subtract(d.descuento()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<ReservacionConfirmadaEvent.HabitacionInfo> habitacionesEvent = new ArrayList<>();
            for (int i = 0; i < contexto.getItems().size(); i++) {
                var item = contexto.getItems().get(i);
                int personas = (request.totalPersonas() != null && i < request.totalPersonas().size()) ? request.totalPersonas().get(i) : 2;
                habitacionesEvent.add(new ReservacionConfirmadaEvent.HabitacionInfo(
                        item.getTipoHabitacion() != null ? item.getTipoHabitacion() : "Habitación Estándar",
                        personas
                ));
            }

            ReservacionConfirmadaEvent reservacionConfirmadaEvent = ReservacionConfirmadaEvent.builder()
                    .nombreReserva(request.nombreReserva())
                    .email(request.email())
                    .email2(request.email2())
                    .peticionEspecial(request.peticionEspecial())
                    .membresia(contexto.getMembresia())
                    .fechaEntrada(contexto.getFechaEntrada())
                    .fechaSalida(contexto.getFechaSalida())
                    .desarrollo(contexto.getDesarrollo())
                    .subtotal(subtotal)
                    .foliosGenerados(consecutivosGenerados)
                    .habitaciones(habitacionesEvent)
                    .build();

            eventPublisher.publishEvent(reservacionConfirmadaEvent);
        } catch (Exception e) {
            businessLogger.error("SYSTEM", "Error al publicar el evento de reservación confirmada para folio principal {}: {}", folioPrincipal, e.getMessage());
        }

        // 7. Retornamos la LISTA COMPLETA de folios
        return ApiResponse.success("Reservaciones generadas con éxito.", consecutivosGenerados);
    }



    // =========================================================================
    // 4. MÉTODOS PRIVADOS
    // =========================================================================

    private void validarFechas(java.time.LocalDate entrada, java.time.LocalDate salida) {
        if (entrada.isAfter(salida) || entrada.isEqual(salida)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
    }

    private void validarOcupantesVsHabitaciones(List<Integer> personasRequest, int cantidadHabitaciones) {
        if (personasRequest != null && personasRequest.size() != cantidadHabitaciones) {
            throw new IllegalArgumentException("La lista de ocupantes no coincide con las habitaciones reservadas.");
        }
    }

    private ReservacionContexto obtenerContextoHidratado(UUID groupId) {
        String jsonContexto = repository.obtenerContextoReservaTemporalJson(groupId);
        if (jsonContexto == null) throw new IllegalArgumentException("El carrito expiró o ya fue procesado.");

        return JsonUtils.fromJson(jsonContexto, new TypeReference<ReservacionContexto>() {
        });
    }

    private ResultadoBeneficio evaluarBeneficios(CalcularCheckoutRequest.CuponRequest cupon, String codigoPromocion, ReservacionContexto contexto, List<ResumenCheckoutResponse.ItemCheckoutDto> habitacionesVisuales) {
        ResultadoBeneficio beneficio = evaluarBeneficiosSobreContexto(cupon, codigoPromocion, contexto);

        if (beneficio.esValido() && contexto != null && contexto.getUnidadElegidaParaDescuento() != null) {
            Integer idHabitacionGanadora = contexto.getUnidadElegidaParaDescuento().getIdTipoHabitacion();

            for (var hab : habitacionesVisuales) {
                if (hab.getIdTipoHabitacion().equals(idHabitacionGanadora)) {
                    hab.setDescuentoAplicado(beneficio.montoDescuento());
                    hab.setMotivoDescuento(beneficio.mensajeMotivoVisual());
                    break;
                }
            }
        }
        return beneficio;
    }

    private ResultadoBeneficio evaluarBeneficiosSobreContexto(CalcularCheckoutRequest.CuponRequest cupon, String codigoPromocion, ReservacionContexto contexto) {
        if (contexto == null || contexto.getItems().isEmpty())
            return new ResultadoBeneficio(BigDecimal.ZERO, false, null, null, null);

        if (cupon != null) {
            contexto.setUnidadElegidaParaDescuento(contexto.getItems().getFirst());
            BigDecimal porcentaje = cupon.porcentajeDescuento().divide(new BigDecimal("100"));
            BigDecimal descuento = contexto.getUnidadElegidaParaDescuento().getCostoEstancia().multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);

            if (descuento.compareTo(BigDecimal.ZERO) > 0) {
                return new ResultadoBeneficio(descuento, true, "CUPON", "Cupón PQA: " + cupon.tipoDescuento(), cupon.tipoDescuento());
            }
            return new ResultadoBeneficio(BigDecimal.ZERO, false, null, "El cupón no arrojó descuento aplicable.", null);
        }

        if (codigoPromocion != null && !codigoPromocion.isBlank()) {
            Promocion promocion = promocionesService.validarCodigoInterno(codigoPromocion);
            if (promocion != null) {
                BigDecimal descuento = promocionesEngine.evaluarYAplicar(promocion, contexto);
                if (descuento.compareTo(BigDecimal.ZERO) > 0) {
                    return new ResultadoBeneficio(descuento, true, "PROMOCION", "¡Promoción aplicada con éxito!", "Promo: " + codigoPromocion.toUpperCase());
                }
                return new ResultadoBeneficio(BigDecimal.ZERO, false, null, "El cupón no aplica para las habitaciones seleccionadas.", null);
            }
            return new ResultadoBeneficio(BigDecimal.ZERO, false, null, "El código ingresado no existe o expiró.", null);
        }

        return new ResultadoBeneficio(BigDecimal.ZERO, false, null, null, null);
    }

    private List<DetalleReservacionJson> generarListaDetallesParaBaseDatos(
            ReservacionContexto contexto,
            List<Integer> totalPersonas,
            BigDecimal descuentoAprobadoTradicional,
            List<Integer> rrtIdsPagoPuntos) {

        List<DetalleReservacionJson> lista = new ArrayList<>();

        for (int i = 0; i < contexto.getItems().size(); i++) {
            var item = contexto.getItems().get(i);
            int personas = (totalPersonas != null && i < totalPersonas.size()) ? totalPersonas.get(i) : 2;
            BigDecimal importeOriginal = item.getCostoEstancia();
            BigDecimal descuentoItem = BigDecimal.ZERO;
            String observacionPagoPuntos = null;

            // CASO A: Pago con Puntos (100% descuento para esa habitación)
            if (rrtIdsPagoPuntos != null && rrtIdsPagoPuntos.contains(item.getRrtId())) {
                descuentoItem = importeOriginal; // Se descuenta el 100% de la tarifa
                observacionPagoPuntos = "PAGO_CON_PUNTOS";
            }
            // CASO B: Promoción Tradicional (Solo si es la unidad ganadora)
            else if (item.equals(contexto.getUnidadElegidaParaDescuento())) {
                descuentoItem = descuentoAprobadoTradicional;
            }

            lista.add(new DetalleReservacionJson(item.getRrtId(), personas, importeOriginal, descuentoItem, observacionPagoPuntos));
        }
        return lista;
    }

    private void quemarBeneficiosEnBaseDeDatos(String tipoAplicado, String membresia, Integer consecutivo, String codigoPromo, CalcularCheckoutRequest.CuponRequest cupon, String usuario) {
        if ("PROMOCION".equals(tipoAplicado)) {
            repository.registrarConsumoPromocion(membresia, consecutivo, codigoPromo, usuario);
        } else if ("CUPON".equals(tipoAplicado)) {
            repository.consumirCuponReservacion(membresia, cupon.paqueteId(), cupon.consecutivo(), usuario);
        }
    }

    // =========================================================================
    // RECORD DE TRANSFERENCIA
    // =========================================================================
    private record ResultadoBeneficio(
            BigDecimal montoDescuento,
            boolean esValido,
            String tipoAplicado,
            String mensaje,
            String mensajeMotivoVisual
    ) {
    }

    /**
     * Obtiene el detalle enriquecido de una reservación.
     */
    public ApiResponse<DetalleReservacionDto> obtenerDetalleReservacion(String membresia, Integer consecutivo) {
        if (membresia == null || membresia.isBlank() || consecutivo == null) {
            throw new IllegalArgumentException("La membresía y el consecutivo son obligatorios.");
        }

        DetalleReservacionDto detalle = repository.obtenerDetalleReservacion(membresia, consecutivo);

        return ApiResponse.success("Detalle de reservación obtenido con éxito.", detalle);
    }

    /**
     * Consulta el resumen general de una reservación específica.
     */
    public ApiResponse<ResumenReservacionDto> obtenerResumenReservacion(String membresia, Integer consecutivo) {
        if (membresia == null || membresia.isBlank() || consecutivo == null) {
            throw new IllegalArgumentException("La membresía y el consecutivo son obligatorios.");
        }

        ResumenReservacionDto resumen = repository.obtenerResumenReservacion(membresia, consecutivo);

        return ApiResponse.success("Resumen de reservación obtenido con éxito.", resumen);
    }

    /**
     * Consulta el estado de cuenta detallado de una reservación específica.
     */
    public ApiResponse<List<CargoHabitacionDto>> obtenerCargosReservacion(String membresia, Integer consecutivo) {
        if (membresia == null || membresia.isBlank() || consecutivo == null) {
            throw new IllegalArgumentException("La membresía y el consecutivo son obligatorios.");
        }

        List<CargoHabitacionDto> cargos = repository.obtenerCargosReservacion(membresia, consecutivo);

        return ApiResponse.success("Historial de cargos obtenido con éxito.", cargos);
    }

    public ApiResponse<DisponibilidadUnidadDto> obtenerDisponibilidadUnidad(Integer rhdtId, String membresia, LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (rhdtId == null || rhdtId <= 0) {
            throw new IllegalArgumentException("El ID del tipo de unidad es obligatorio.");
        }

        DisponibilidadUnidadProjection projection = repository.obtenerDisponibilidadUnidadEspecifica(rhdtId, fechaEntrada, fechaSalida, membresia);

        if (projection == null) {
            return ApiResponse.success("No se encontró información para el tipo de unidad solicitado.", null);
        }

        String imagenUrl = projection.uuidImagen() != null ? storageClient.obtenerUrlDescarga(projection.uuidImagen()) : null;

        DisponibilidadUnidadDto dto = DisponibilidadUnidadDto.builder()
                .idTipoUnidad(projection.idTipoUnidad())
                .nombreUnidad(projection.nombreUnidad())
                .descripcionCorta(projection.descripcionCorta())
                .capacidad(projection.capacidad())
                .stockDisponible(projection.stockDisponible())
                .costoEstancia(projection.costoEstancia())
                .urlImagen(imagenUrl)
                .build();

        return ApiResponse.success("Disponibilidad de unidad obtenida exitosamente", dto);
    }

    public void actualizarReservacionPagada(String membresia, Integer consecutivo) {
        repository.spResvActualizarReservacionPagada(membresia, consecutivo);
    }

    // =========================================================================
    // METODOS DE CONSULTA GENERAL Y RECEPCION REFACTOR
    // =========================================================================

    public ApiResponse<PaginaResponse<ReservacionHistoricaDto>> consultarHistorico(FiltroConsultaGeneral filtro) {
        var filtroConDesarrollo = new FiltroConsultaGeneral(
                userContext.getIdDesarrollo(),
                filtro.fechaInicio(),
                filtro.fechaFin(),
                filtro.tipoFecha(),
                filtro.estatusClave(),
                filtro.busqueda(),
                filtro.pageNumber(),
                filtro.pageSize()
        );

        List<ReservacionHistoricaDto> resultados = repository.consultarHistoricoReservaciones(filtroConDesarrollo);

        Integer totalRegistros = resultados.isEmpty() ? 0 : resultados.getFirst().totalRegistros();

        PaginaResponse<ReservacionHistoricaDto> pagina = new PaginaResponse<>(
                resultados,
                totalRegistros,
                filtro.pageNumber(),
                filtro.pageSize()
        );

        return ApiResponse.success("Consulta histórica completada", pagina);
    }

    public ApiResponse<List<OperacionDiaDto>> obtenerOperacionesDelDia() {
        Integer desarrolloId = userContext.getIdDesarrollo();

        if (desarrolloId == null || desarrolloId <= 0) {
            throw new IllegalArgumentException("El desarrollo asignado a su usuario no permite el acceso a esta información. Contacte al administrador del sistema.");
        }

        List<OperacionDiaDto> operaciones = repository.obtenerOperacionesDelDia(desarrolloId);
        return ApiResponse.success("Operaciones del día obtenidas", operaciones);
    }

    public ApiResponse<List<EstadisticaDelDiaDto>> obtenerEstadisticasDelDia() {
        Integer desarrolloId = userContext.getIdDesarrollo();

        if (desarrolloId == null || desarrolloId <= 0) {
            throw new IllegalArgumentException("El desarrollo asignado a su usuario no permite el acceso a esta información. Contacte al administrador del sistema.");
        }

        List<EstadisticaDelDiaDto> estadisticas = repository.obtenerEstadisticasDelDia(desarrolloId);
        return ApiResponse.success("Estadísticas del día calculadas", estadisticas);
    }

    public ApiResponse<Boolean> registrarCheckIn(CheckInRequest request) {
        String usuario = userContext.getUsername();
        repository.ejecutarCheckIn(request, usuario);
        return ApiResponse.success("Check-In registrado exitosamente", true);
    }

    public ApiResponse<Boolean> registrarCheckOut(CheckOutRequest request) {
        String usuario = userContext.getUsername();

        ResumenReservacionDto detalle = obtenerResumenReservacion(request.membresia(), request.consecutivo()).data();

        repository.ejecutarCheckOut(request, usuario);

        if (detalle.idUnidadFisica() != null) {
            amaDeLlavesService.crearTareaYNotificar(
                    detalle.idUnidadFisica(),
                    detalle.numeroUnidad(),
                    detalle.desarrolloId(),
                    usuario,
                    "CHECK-OUT"
            );
        }

        return ApiResponse.success("Check-Out registrado exitosamente. La habitación ha sido liberada.", true);
    }

    public ApiResponse<CheckInOutEspecialCotizacionDto> cotizarCheckInOutEspecial(String membresia, Integer consecutivo) {
        if (membresia == null || membresia.isBlank() || consecutivo == null) {
            throw new IllegalArgumentException("La membresía y el consecutivo son obligatorios.");
        }

        CheckInOutEspecialCotizacionDto cotizacion = repository.cotizarCheckInOutEspecial(membresia, consecutivo);

        if (cotizacion == null) {
            return ApiResponse.error(GeneralResponseCode.NOT_FOUND, "No se encontró la reservación especificada.");
        }

        return ApiResponse.success("Cotización de operación especial obtenida.", cotizacion);
    }

    public ApiResponse<Boolean> registrarMovimientoCheckInOutEspecial(CheckInOutEspecialRequest request) {
        String usuario = userContext.getUsername();

        repository.registrarMovimientoCheckInOutEspecial(request, usuario);

        businessLogger.info(usuario, "Cargo por {} registrado: Membresía {}, Consecutivo {}",
                request.tipoOperacion(), request.membresia(), request.consecutivo());
        return ApiResponse.success("Cargo por " + request.tipoOperacion().name() + " registrado correctamente.", true);
    }

    public ApiResponse<List<UnidadDisponibleDto>> obtenerUnidadesDisponiblesCheckIn(String membresia, Integer consecutivo, Integer rhdtId) {
        if (rhdtId == null || rhdtId <= 0) {
            throw new IllegalArgumentException("El ID del tipo de unidad es obligatorio.");
        }
        if (membresia == null || membresia.isBlank() || consecutivo == null) {
            throw new IllegalArgumentException("La membresía y el consecutivo son obligatorios para validar la disponibilidad real.");
        }

        List<UnidadDisponibleDto> unidades = repository.obtenerUnidadesDisponiblesParaCheckIn(membresia, consecutivo, rhdtId);
        return ApiResponse.success("Unidades disponibles obtenidas", unidades);
    }

    public ApiResponse<List<CatalogoCargoDto>> obtenerCatalogoCargos(String membresia) {
        if (membresia == null || membresia.isBlank()) {
            throw new IllegalArgumentException("La membresía es obligatoria.");
        }

        List<CatalogoCargoDto> catalogo = repository.obtenerCatalogoCargosHabitacion(membresia);
        return ApiResponse.success("Catálogo de cargos obtenido.", catalogo);
    }

    public ApiResponse<Boolean> generarCargo(GenerarCargoRequest request) {
        String usuario = userContext.getUsername();

        repository.generarCargoHabitacion(request, usuario);

        return ApiResponse.success("Cargo aplicado a la habitación exitosamente.", true);
    }

    public ApiResponse<List<MapaUnidadDto>> obtenerMapaUnidades() {
        Integer desarrolloId = userContext.getIdDesarrollo();

        List<MapaUnidadDto> mapa = repository.obtenerMapaUnidades(desarrolloId);
        return ApiResponse.success("Mapa de unidades obtenido con éxito.", mapa);
    }

    public ApiResponse<List<String>> obtenerActividadReciente() {
        Integer desarrolloId = userContext.getIdDesarrollo();

        List<String> actividad = repository.obtenerActividadDiaria(desarrolloId);
        return ApiResponse.success("Actividad reciente obtenida.", actividad);
    }

    public ApiResponse<Boolean> transferirUnidad(TransferirUnidadRequest request) {
        String usuario = userContext.getUsername();

        ResumenReservacionDto detalle = obtenerResumenReservacion(request.membresia(), request.consecutivo()).data();

        repository.transferirUnidad(request, usuario);

        businessLogger.info(usuario, "Transferencia de habitación realizada para Membresía: {}, Consecutivo: {}, Nuevo RHDT ID: {}, Nuevo RUN ID: {}, Importe Diferencia: {}",
                request.membresia(), request.consecutivo(), request.nuevoRhdtId(), request.nuevoRunId(), request.importeDiferencia());

        if (detalle.idUnidadFisica() != null) {
            amaDeLlavesService.crearTareaYNotificar(
                    detalle.idUnidadFisica(),
                    detalle.numeroUnidad(),
                    detalle.desarrolloId(),
                    usuario,
                    "TRANSFERENCIA-HABITACION"
            );
        }

        return ApiResponse.success("La transferencia de habitación se realizó con éxito.", true);
    }

    public ApiResponse<BigDecimal> calcularDiferenciaTransferencia(String membresia, Integer consecutivo, Integer nuevoRhdtId) {
        DetalleReservacionDto detalle = obtenerDetalleReservacion(membresia, consecutivo).data();

        DisponibilidadUnidadDto disponibilidad = obtenerDisponibilidadUnidad(
                nuevoRhdtId,
                detalle.membresia(),
                detalle.fechaEntrada(),
                detalle.fechaSalida()
        ).data();

        BigDecimal diferencia = getBigDecimal(disponibilidad, detalle);

        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La nueva unidad seleccionada tiene una tarifa menor a la actual. Por favor, seleccione una unidad con tarifa igual o mayor.");
        }

        return ApiResponse.success("Diferencia de tarifa calculada.", diferencia);
    }

    private static BigDecimal getBigDecimal(DisponibilidadUnidadDto disponibilidad, DetalleReservacionDto detalle) {
        if (disponibilidad.capacidad() < detalle.numeroSocios()) {
            throw new IllegalArgumentException("La nueva unidad no tiene la capacidad suficiente para alojar a la cantidad de personas de la reservación original.");
        }

        BigDecimal tarifaActual = detalle.importeTotal();
        BigDecimal tarifaNueva = disponibilidad.costoEstancia();

        BigDecimal diferencia = tarifaNueva.subtract(tarifaActual);
        return diferencia;
    }

    public ApiResponse<BigDecimal> calcularPenalizacionCancelacion(String membresia, Integer consecutivo) {
        BigDecimal penalizacion = repository.calcularPenalizacionCancelacion(membresia, consecutivo);
        return ApiResponse.success("Cálculo de penalización completado.", penalizacion);
    }

    public ApiResponse<Boolean> cancelarReservacion(CancelarReservacionRequest request, String usuario) {
        ResumenReservacionDto detalle = obtenerResumenReservacion(request.membresia(), request.consecutivo()).data();

        repository.cancelarReservacion(request, usuario);

        businessLogger.info(usuario, "Cancelación de reservación realizada para Membresía: {}, Consecutivo: {}, Motivo: {}, Cobro Penalización: {}",
                request.membresia(), request.consecutivo(), request.motivoCancelacion(), request.cobrarCuotaCancelacion());

        BigDecimal costoCancelacion = request.cobrarCuotaCancelacion() ? repository.calcularPenalizacionCancelacion(request.membresia(), request.consecutivo()) : BigDecimal.ZERO;

        enviarCorreoCancelacion(detalle, costoCancelacion);

        return ApiResponse.success("La reservación ha sido cancelada y la habitación liberada con éxito.", true);
    }

    private void enviarCorreoCancelacion(ResumenReservacionDto detalle, BigDecimal costoCancelacion) {
        String destinatario = detalle.emailContacto();
        String nombreCliente = detalle.nombreContacto();
        String membresia = detalle.membresia();
        Integer consecutivo = detalle.consecutivo();

        Map<String, Object> urlVariables = new HashMap<>();
        urlVariables.put("membresia", membresia);
        urlVariables.put("folio", consecutivo);
        urlVariables.put("nombreTitular", nombreCliente);
        urlVariables.put("costoCancelacion", costoCancelacion.compareTo(BigDecimal.ZERO) > 0 ? costoCancelacion.toString() : null);

        SolicitudNotificacionDto solicitudNotificacion = SolicitudNotificacionDto.builder()
                .aliasConfig(aliasConfigNotificaciones)
                .destinatarios(List.of(destinatario))
                .codigoPlantilla(templateReservaCancelada)
                .variables(urlVariables)
                .prioridad(10)
                .build();

        notificationClient.enviarNotificacion(solicitudNotificacion);
    }

    public ResumenReservacionDto obtenerResumenReservacionXMovimiento(String membresia, Integer movimiento) {
        return repository.spResvObtenerReservacionXMovimiento(membresia, movimiento)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información para la reservación solicitada."));
    }
}