package com.coralclubes.facil.modules.cobranza.model.pagos.strategies;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.ProcesarPagoResponse;
import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    public ProcesarPagoResponse procesar(UUID ordenUuid, ProcesarPagoRequest request) {

        // 1. Aquí simulamos una llamada a la API de la Terminal con timeout de 3 segundos
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("El procesamiento del pago fue interrumpido.", e);
        }

        String metadataInicial = "{\"esperandoTerminal\": true}";

        // 2. Queda PENDIENTE hasta que el Webhook o el Polling confirmen
        Integer intentoId = intentoPagoRepository.spCobranzaRegistrarIntentoPago(
                ordenUuid, request.formaPagoClave(), request.monto(), "PENDIENTE", metadataInicial
        ).orElseThrow();

        log.info("Intento de pago con TARJETA, orden {} registrado con ID {} y estatus PENDIENTE", ordenUuid, intentoId);

        return ProcesarPagoResponse.builder()
                .intentoPagoId(intentoId)
                .estatus("PENDIENTE")
                .mensajeAccion("Deslice o inserte la tarjeta en la terminal física.")
                .build();
    }
}