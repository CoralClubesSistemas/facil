package com.coralclubes.facil.modules.cobranza.model.pagos.strategies;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.ProcesarPagoResponse;
import com.coralclubes.facil.modules.cobranza.model.pagos.enums.EstatusIntentoPago;
import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalPaymentStrategy implements PaymentStrategy {

    private final IntentoPagoRepository intentoPagoRepository;

    @Override
    public String getGatewayType() {
        return "TARJETA";
    }

    @Override
    public ProcesarPagoResponse procesar(UUID ordenUuid, ProcesarPagoRequest request, String usuario) {
        String estatus = EstatusIntentoPago.APROBADO.toString();

        String metadata = request.metadata() != null ? JsonUtils.toJson(request.metadata()) : null;

        Integer intentoId = intentoPagoRepository.spCobranzaRegistrarIntentoPago(
                ordenUuid, request.formaPagoClave(), request.monto(), estatus, metadata
        ).orElseThrow();

        // 2. Al ser aprobado directo, actualizamos fecha de aprobación
        intentoPagoRepository.spCobranzaActualizarEstatusIntentoPago(intentoId, estatus, LocalDateTime.now());

        log.info("Intento de pago con TARJETA, orden {} registrado con ID {} y estatus APROBADO", ordenUuid, intentoId);

        return ProcesarPagoResponse.builder()
                .intentoPagoId(intentoId)
                .estatus(estatus)
                .mensajeAccion("Pago con tarjeta registrado correctamente")
                .build();
    }

    @Override
    public void postProcesarFinalizacion(Integer idIntentoPago) {
        intentoPagoRepository.spCobranzaRegistrarPagoTarjeta(idIntentoPago);
    }

    @Override
    public void eliminarIntento(UUID ordenUuid, Integer intentoPagoId, IntentoPagoDto intento, String usuario) {
        intentoPagoRepository.spCobranzaEliminarIntentoPago(ordenUuid, intentoPagoId);
    }
}