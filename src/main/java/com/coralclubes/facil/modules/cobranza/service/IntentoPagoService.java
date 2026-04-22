package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.ConsultarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCumplimientoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.ProcesarPagoResponse;
import com.coralclubes.facil.modules.cobranza.model.pagos.engine.PaymentStrategyFactory;
import com.coralclubes.facil.modules.cobranza.model.pagos.enums.EstatusIntentoPago;
import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntentoPagoService {
    private final PaymentStrategyFactory factory;
    private final IntentoPagoRepository intentoPagoRepository;
    private final CobranzaService cobranzaService;
    private final UserContext userContext;
    private final BusinessLogger logger;

    @Transactional
    public ApiResponse<ProcesarPagoResponse> iniciarPago(UUID ordenUuid, ProcesarPagoRequest request) {

        // 1. Obtenemos la estrategia dinámica basada en el ID (Efectivo, Tarjeta, Link...)
        PaymentStrategy strategy = factory.getStrategy(request.formaPagoClave());

        // 2. Ejecutamos la lógica del intento de pago
        ProcesarPagoResponse response = strategy.procesar(ordenUuid, request);

        return ApiResponse.success("Intento de pago procesado", response);
    }

    /**
     * Evalúa el cumplimiento de la orden y devuelve el historial de transacciones para reconstruir el UI.
     */
    public ApiResponse<EstadoCumplimientoDto> evaluarCumplimientoDeOrden(UUID ordenUuid) {
        // 1. Obtenemos todas las transacciones desde la BD
        List<IntentoPagoDto> intentos = intentoPagoRepository.spCobranzaObtenerIntentosPagoPorOrden(ordenUuid);

        // 2. Sumamos el dinero real aprobado
        BigDecimal totalAprobado = intentos.stream()
                .filter(i -> EstatusIntentoPago.APROBADO.toString().equals(i.estatus()))
                .map(IntentoPagoDto::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Consultamos el total de la orden
        ConsultarOrdenCobranzaResponse orden = cobranzaService.consultarOrdenCobranza(ordenUuid).data();
        BigDecimal totalEsperado = orden.totalPagar();

        // 4. Cálculos finales
        BigDecimal saldoPendiente = totalEsperado.subtract(totalAprobado).max(BigDecimal.ZERO);
        boolean isCompletado = totalAprobado.compareTo(totalEsperado) >= 0;

        EstadoCumplimientoDto respuesta = EstadoCumplimientoDto.builder()
                .isCompletado(isCompletado)
                .totalEsperado(totalEsperado)
                .totalAprobado(totalAprobado)
                .saldoPendiente(saldoPendiente)
                .transacciones(intentos) // Mandamos la lista para reconstruir el frontend
                .build();

        return ApiResponse.success("Estado de pagos consultado", respuesta);
    }

    /**
     * Elimina un pago específico y devuelve el nuevo estado de la orden.
     */
    @Transactional
    public ApiResponse<EstadoCumplimientoDto> eliminarPago(UUID ordenUuid, Integer intentoPagoId) {
        String usuario = userContext.getUsername();

        // 1. Eliminamos el registro (El SP valida las reglas de negocio)
        intentoPagoRepository.spCobranzaEliminarIntentoPago(ordenUuid, intentoPagoId);

        // 2. Re-evaluamos la orden para devolver los nuevos totales actualizados
        ApiResponse<EstadoCumplimientoDto> nuevoEstado = evaluarCumplimientoDeOrden(ordenUuid);

        logger.info("Pago eliminado por {}: orden={}, intentoPagoId={}", usuario, ordenUuid, intentoPagoId);

        return ApiResponse.success("Pago eliminado correctamente", nuevoEstado.data());
    }
}
