package com.coralclubes.facil.modules.cobranza.model.pagos.strategies;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
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
public class EfectivoPaymentStrategy implements PaymentStrategy {

    private final IntentoPagoRepository intentoPagoRepository;

    @Override
    public String getGatewayType() {
        return "EFECTIVO";
    }

    @Override
    public ProcesarPagoResponse procesar(UUID ordenUuid, ProcesarPagoRequest request) {
        // 1. Efectivo se aprueba inmediatamente porque el cajero tiene el dinero en mano
        String estatus = EstatusIntentoPago.APROBADO.toString();

        String metadata = request.metadata() != null ? JsonUtils.toJson(request.metadata()) : null;

        Integer intentoId = intentoPagoRepository.spCobranzaRegistrarIntentoPago(
                ordenUuid, request.formaPagoClave(), request.monto(), estatus, metadata
        ).orElseThrow();

        // 2. Al ser aprobado directo, actualizamos fecha de aprobación
        intentoPagoRepository.spCobranzaActualizarEstatusIntentoPago(intentoId, estatus, LocalDateTime.now());

        log.info("Intento de pago con EFECTIVO, orden {} registrado con ID {} y estatus APROBADO", ordenUuid, intentoId);

        return ProcesarPagoResponse.builder()
                .intentoPagoId(intentoId)
                .estatus(estatus)
                .mensajeAccion("Pago en efectivo registrado correctamente.")
                .build();
    }

    @Override
    public void postProcesarFinalizacion(Integer idIntentoPago) {
        intentoPagoRepository.spCobranzaRegistrarPagoEfectivo(idIntentoPago);
    }

    @Override
    public void eliminarIntento(UUID ordenUuid, Integer intentoPagoId, com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto intento) {
        intentoPagoRepository.spCobranzaEliminarIntentoPago(ordenUuid, intentoPagoId);
    }
}