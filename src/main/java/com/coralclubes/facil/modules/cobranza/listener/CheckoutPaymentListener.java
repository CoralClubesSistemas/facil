package com.coralclubes.facil.modules.cobranza.listener;

import com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.shared.events.dto.CheckoutPaymentStatusChangedEvent;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutPaymentListener {

    private final IntentoPagoRepository intentoPagoRepository;
    private final CobranzaService cobranzaService;

    @Value("${app.clients.checkout.default-tipo-serie-recibo:1}")
    private Integer defaultTipoSerieRecibo;

    @ApplicationModuleListener
    public void handleCheckoutPaymentStatusChanged(CheckoutPaymentStatusChangedEvent event) {
        log.info("[COBRANZA] Recibido evento de pago de Checkout. Orden: {}, Transacción: {}, Estatus: {}",
                event.externalReference(), event.transactionUuid(), event.status());

        if (!"APROBADO".equalsIgnoreCase(event.status())) {
            log.warn("[COBRANZA] El pago no está APROBADO (Estatus: {}). Omitiendo.", event.status());
            return;
        }

        UUID ordenUuid;
        try {
            ordenUuid = UUID.fromString(event.externalReference());
        } catch (IllegalArgumentException e) {
            log.error("[COBRANZA] ExternalReference no es un UUID válido: {}", event.externalReference());
            return;
        }

        // 1. Obtener los intentos de pago asociados a la orden
        List<IntentoPagoDto> intentos = intentoPagoRepository.spCobranzaObtenerIntentosPagoPorOrden(ordenUuid);

        // 2. Buscar el intento de pago correspondiente por checkoutUuid
        Optional<IntentoPagoDto> intentoOpt = intentos.stream()
                .filter(i -> "LINK".equalsIgnoreCase(i.formaPagoClave()))
                .filter(i -> {
                    if (i.metadata() == null || i.metadata().isBlank()) return false;
                    try {
                        Map<String, Object> meta = JsonUtils.fromJson(i.metadata(), Map.class);
                        return event.transactionUuid().equals(meta.get("checkoutUuid"));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();

        if (intentoOpt.isEmpty()) {
            log.error("[COBRANZA] No se encontró ningún intento de pago LINK para checkoutUuid: {} en la orden: {}",
                    event.transactionUuid(), ordenUuid);
            return;
        }

        IntentoPagoDto intento = intentoOpt.get();

        try {
            log.info("[COBRANZA] Actualizando intento de pago ID: {} a APROBADO.", intento.intentoPagoId());

            // 3. Actualizar el estatus del intento a APROBADO en la base de datos
            intentoPagoRepository.spCobranzaActualizarEstatusIntentoPago(intento.intentoPagoId(), "APROBADO", LocalDateTime.now());

            // 4. Finalizar la orden de cobranza y generar el recibo (esto publicará ReciboPagadoEvent)
            String emailCliente = event.metadata() != null ? (String) event.metadata().get("payerEmail") : null;
            if (emailCliente == null) {
                try {
                    var ordenInfo = cobranzaService.consultarOrdenCobranza(ordenUuid).data();
                    emailCliente = ordenInfo.correo();
                } catch (Exception ex) {
                    log.warn("[COBRANZA] No se pudo consultar la orden de cobranza para extraer el correo del cliente: {}", ex.getMessage());
                }
            }

            List<String> correos = emailCliente != null && !emailCliente.isBlank() ? List.of(emailCliente) : List.of();

            log.info("[COBRANZA] Finalizando orden de cobranza {} y generando recibo con tipo de serie {}", ordenUuid, defaultTipoSerieRecibo);
            cobranzaService.finalizarOrdenYGenerarRecibo(
                    ordenUuid.toString(),
                    defaultTipoSerieRecibo,
                    "INTERNET",
                    correos
            );

            log.info("[COBRANZA] Orden de cobranza {} finalizada y pagada con éxito tras la confirmación de Checkout.", ordenUuid);

        } catch (Exception e) {
            log.error("[COBRANZA] Error al finalizar la orden de cobranza " + ordenUuid, e);
            throw e;
        }
    }
}
