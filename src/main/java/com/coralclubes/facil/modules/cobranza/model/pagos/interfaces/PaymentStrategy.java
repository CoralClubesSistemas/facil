package com.coralclubes.facil.modules.cobranza.model.pagos.interfaces;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.ProcesarPagoResponse;

import java.util.UUID;

public interface PaymentStrategy {

    // Identifica qué tipo de pagos procesa esta estrategia
    String getGatewayType();

    // La lógica core de ejecución
    ProcesarPagoResponse procesar(UUID ordenUuid, ProcesarPagoRequest request);

    // Lógica que se ejecuta SOLO cuando la orden ya se facturó/cerró exitosamente
    void postProcesarFinalizacion(Integer idIntentoPago);

    // Lógica para eliminar un intento de pago
    void eliminarIntento(UUID ordenUuid, Integer intentoPagoId, IntentoPagoDto intento);
}