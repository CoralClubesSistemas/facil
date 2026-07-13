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
import com.coralclubes.facil.modules.usuarios.service.UserContext;
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
    public ProcesarPagoResponse iniciarPago(UUID ordenUuid, ProcesarPagoRequest request, String usuario) {
        // 1. Obtenemos la estrategia dinámica basada en el ID (Efectivo, Tarjeta, Link...)
        PaymentStrategy strategy = factory.getStrategy(request.formaPagoClave());

        // 2. Ejecutamos la lógica del intento de pago
        return strategy.procesar(ordenUuid, request, usuario);
    }

    /**
     * Evalúa el cumplimiento de la orden y devuelve el historial de transacciones para reconstruir el UI.
     */
    public EstadoCumplimientoDto evaluarCumplimientoDeOrden(UUID ordenUuid) {
        // 1. Obtenemos todas las transacciones desde la BD
        List<IntentoPagoDto> intentos = intentoPagoRepository.spCobranzaObtenerIntentosPagoPorOrden(ordenUuid);

        // 2. Sumamos el dinero real aprobado
        BigDecimal totalAprobado = intentos.stream()
                .filter(i -> EstatusIntentoPago.APROBADO.toString().equals(i.estatus()))
                .map(IntentoPagoDto::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sumamos el total de dinero acumulado
        BigDecimal totalAcumulado = intentos.stream()
                .map(IntentoPagoDto::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Consultamos el total de la orden
        ConsultarOrdenCobranzaResponse orden = cobranzaService.consultarOrdenCobranza(ordenUuid).data();
        BigDecimal totalEsperado = orden.totalPagar();

        // 4. Cálculos finales
        BigDecimal saldoPendiente = totalEsperado.subtract(totalAprobado).max(BigDecimal.ZERO);
        boolean isCompletado = totalAcumulado.compareTo(totalEsperado) >= 0;

        return EstadoCumplimientoDto.builder()
                .isCompletado(isCompletado)
                .totalEsperado(totalEsperado)
                .totalAprobado(totalAprobado)
                .saldoPendiente(saldoPendiente)
                .transacciones(intentos) // Mandamos la lista para reconstruir el frontend
                .build();
    }

    /**
     * Elimina un pago específico y devuelve el nuevo estado de la orden.
     */
    @Transactional
    public EstadoCumplimientoDto eliminarPago(UUID ordenUuid, Integer intentoPagoId, String usuario) {
        // 1. Obtener los intentos de pago asociados a la orden
        List<IntentoPagoDto> intentos = intentoPagoRepository.spCobranzaObtenerIntentosPagoPorOrden(ordenUuid);
        IntentoPagoDto intento = intentos.stream()
                .filter(i -> i.intentoPagoId().equals(intentoPagoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el intento de pago con ID: " + intentoPagoId));

        // 2. Resolver estrategia y delegar eliminación
        PaymentStrategy strategy = factory.getStrategy(intento.formaPagoClave());
        strategy.eliminarIntento(ordenUuid, intentoPagoId, intento, usuario);

        // 3. Re-evaluamos la orden para devolver los nuevos totales actualizados
        EstadoCumplimientoDto nuevoEstado = evaluarCumplimientoDeOrden(ordenUuid);

        logger.info("Pago eliminado por {}: orden={}, intentoPagoId={}", usuario, ordenUuid, intentoPagoId);

        return nuevoEstado;
    }
}
