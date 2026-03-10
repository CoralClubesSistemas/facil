package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.clientes.dto.request.ConsumoPuntosRequest;
import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleDto;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosMembresia;
import com.coralclubes.facil.modules.clientes.service.PuntosService;
import com.coralclubes.facil.modules.reservaciones.dto.projection.DisponibilidadUnidadProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.DisponibilidadUnidadDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.OpcionPagoPuntosDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion;
import com.coralclubes.facil.modules.reservaciones.dto.response.ResumenCheckoutResponse;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.engine.PromocionesEngine;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private final GeneradorDocumentosService generadorDocumentosService;
    private final NotificationClient notificationClient;

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
        boolean exito = repository.eliminarReservaTemporal(groupId);
        if (!exito) {
            return ApiResponse.success("El inventario ya estaba liberado o no se encontró.", false);
        }
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
        BigDecimal baseGravable = habitaciones.stream().map(ResumenCheckoutResponse.ItemCheckoutDto::getTotalFinalHabitacion).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = baseGravable.multiply(new BigDecimal("0.16")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAPagar = baseGravable.add(iva);

        // 7. Ensamblaje
        var resumen = ResumenCheckoutResponse.ResumenFinancieroDto.builder()
                .subtotalOriginal(subtotalOriginal)
                .totalDescuentos(subtotalOriginal.subtract(baseGravable)) // La diferencia entre el original y lo que quedó gravable
                .baseGravable(baseGravable)
                .iva(BigDecimal.ZERO)
                .ish(BigDecimal.ZERO)
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
        // 6. CONSUMIR PUNTOS A TRAVÉS DEL MÓDULO DE CLIENTES
        // ====================================================================
        if (request.rrtIdsPagoPuntos() != null && !request.rrtIdsPagoPuntos().isEmpty()) {

            // Iteramos solo las habitaciones que se seleccionaron para pagar con puntos
            for (OpcionPagoPuntosDto opcion : opcionesPuntos) {
                if (request.rrtIdsPagoPuntos().contains(opcion.rrtId())) {

                    // Buscamos a qué desarrollo (hotel) pertenece esta habitación desde el contexto
                    Integer desarrolloId = contexto.getIdDesarrollo();

                    if (desarrolloId != null) {
                        // Construimos el DTO agnóstico para enviarlo a Clientes
                        ConsumoPuntosRequest peticionPuntos = ConsumoPuntosRequest.builder()
                                        .membresia(contexto.getMembresia())
                                        .desarrolloId(desarrolloId)
                                        .totalPuntos(opcion.costoTotalPuntos())

                                        // Asignamos el 100% de los puntos al rubro de Hospedaje
                                        .puntosHospedaje(opcion.costoTotalPuntos())
                                        .puntosInstalaciones(0)
                                        .puntosCampoGolf(0)

                                        .idMovimiento(folioPrincipal) // Enlazamos con la reserva que acaba de nacer
                                        .descripcion("RESERVA CON PUNTOS - " + opcion.nombrePromocion())
                                        .usuario(usuario)
                                        .build();

                        // Delegamos el descuento de puntos
                        puntosService.consumirPuntos(peticionPuntos);
                    }
                }
            }
        }

        try {
            // Calculamos matemáticamente el importe final que verá en su recibo
            BigDecimal subtotal = listaDetalles.stream()
                    .map(d -> d.importeOriginal().subtract(d.descuento()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            generarYEnviarCartaOcupacion(request, contexto, consecutivosGenerados, subtotal);
        } catch (Exception e) {
            businessLogger.error("SYSTEM", "Error al generar o enviar la Carta de Ocupación para la reserva con folio principal {}: {}", folioPrincipal, e.getMessage());
        }

        // 7. Retornamos la LISTA COMPLETA de folios
        return ApiResponse.success("Reservaciones generadas con éxito.", consecutivosGenerados);
    }

    private void generarYEnviarCartaOcupacion(
            ConfirmarReservaRequest request,
            ReservacionContexto contexto,
            List<Integer> foliosGenerados,
            BigDecimal importeTotal
    ) {

        // 1. Formatear la lista de habitaciones para el DTO del PDF
        List<DatosCartaOcupacionDto.HabitacionCartaDto> habitacionesPdf = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (int i = 0; i < contexto.getItems().size(); i++) {
            var item = contexto.getItems().get(i);
            int personas = (request.totalPersonas() != null && i < request.totalPersonas().size()) ? request.totalPersonas().get(i) : 2;

            habitacionesPdf.add(DatosCartaOcupacionDto.HabitacionCartaDto.builder()
                    .tipoHabitacion(item.getTipoHabitacion() != null ? item.getTipoHabitacion() : "Habitación Estándar")
                    .totalPax(personas)
                    .build());
        }

        String foliosStr = foliosGenerados.toString().replace("[", "").replace("]", "");

        // 2. Construir DTO del Generador
        DatosCartaOcupacionDto datosPdf = DatosCartaOcupacionDto.builder()
                .fechaEmision(LocalDate.now().format(formatter))
                .titular(request.nombreReserva())
                .membresia(contexto.getMembresia() != null ? contexto.getMembresia() : "PÚBLICO GENERAL")
                .foliosReservacion(foliosStr)
                .habitaciones(habitacionesPdf)
                .observaciones(request.peticionEspecial())
                .importeTotal(importeTotal)
                .fechaEntrada(contexto.getFechaEntrada().toString())
                .fechaSalida(contexto.getFechaSalida().toString())
                .desarrollo(contexto.getDesarrollo())
                .build();

        // 3. Llamar al Generador de Documentos (Thymeleaf -> PDF -> Storage)
        UUID urlPdfCartaOcupacion = generadorDocumentosService.generarYGuardarCartaOcupacion(datosPdf);

        // 4. Preparar Destinatarios
        List<String> destinatarios = new ArrayList<>();
        destinatarios.add(request.email());
        if (request.email2() != null && !request.email2().isBlank()) {
            destinatarios.add(request.email2());
        }

        // 5. Construir Solicitud a Coral Notificaciones
        SolicitudNotificacionDto solicitudNotificacion = SolicitudNotificacionDto.builder()
                .codigoSistema("FACIL")
                .aliasConfig("SMTP_GENERAL")
                .destinatarios(destinatarios)
                .asunto("Confirmación de Reservación - Folios: " + foliosStr)
                .cuerpo(
                        "Estimado/a " + request.nombreReserva() + ",\n\n" +
                        "Su reservación ha sido confirmada exitosamente. Adjuntamos la carta de ocupación con los detalles de su reserva.\n\n" +
                        "¡Gracias por elegirnos para su próxima estancia!\n\n" +
                        "Saludos cordiales,\n" +
                        "Equipo de Reservaciones"
                )
                .variables(Map.of(
                        "nombreTitular", request.nombreReserva(),
                        "urlDescargaPdf", urlPdfCartaOcupacion
                ))
                .prioridad(10)
                .adjuntos(List.of(urlPdfCartaOcupacion.toString()))
                .build();

        // 6. Enviar a Cola RabbitMQ (Fire and Forget)
        notificationClient.enviarNotificacion(solicitudNotificacion);
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
}