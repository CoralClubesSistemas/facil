package com.coralclubes.facil.modules.cobranza.model.pagos.strategies;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.ProcesarPagoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.ConsultarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.model.pagos.enums.EstatusIntentoPago;
import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.CheckoutClient;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.dto.CheckoutInitRequest;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.dto.CheckoutInitResponse;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkPaymentStrategy implements PaymentStrategy {

    private final IntentoPagoRepository intentoPagoRepository;
    private final CobranzaRepository cobranzaRepository;
    private final CheckoutClient checkoutClient;
    private final ObjectMapper objectMapper;

    @Value("${app.clients.checkout.redirect-success:http://localhost:4200/reserva/exito}")
    private String redirectSuccess;

    @Value("${app.clients.checkout.redirect-failure:http://localhost:4200/reserva/fallo}")
    private String redirectFailure;

    @Value("${app.clients.checkout.redirect-cancel:http://localhost:4200/reserva/detalle}")
    private String redirectCancel;

    @Value("${app.clients.checkout.is-sandbox:true}")
    private Boolean isSandbox;

    @Override
    public String getGatewayType() {
        return "LINK";
    }

    @Override
    public ProcesarPagoResponse procesar(UUID ordenUuid, ProcesarPagoRequest request) {
        // 1. Consultar detalles de la orden directamente desde el JSON del repositorio para evitar dependencias circulares
        String ordenJson = cobranzaRepository.spFacilConsultarOrdenCobranzaJson(ordenUuid)
                .orElseThrow(() -> new IllegalStateException("No se encontró información de la orden de cobranza."));

        ConsultarOrdenCobranzaResponse orden;
        try {
            orden = objectMapper.readValue(ordenJson, ConsultarOrdenCobranzaResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo interpretar el JSON de la orden de cobranza.");
        }

        // 2. Mapear conceptos a ítems de checkout
        List<CheckoutInitRequest.CheckoutItem> checkoutItems = orden.conceptos().stream()
                .map(concepto -> new CheckoutInitRequest.CheckoutItem(
                        concepto.idMovimiento(),
                        concepto.concepto(),
                        1,
                        concepto.subtotal(),
                        concepto.detalle()
                ))
                .toList();

        // 3. Construir la solicitud de checkout
        String expirationDate = LocalDateTime.now().plusDays(1).toString();

        CheckoutInitRequest checkoutRequest = new CheckoutInitRequest(
                ordenUuid.toString(),
                request.monto(),
                "MXN",
                "Pago de orden de cobranza " + orden.numeroOrden(),
                "test_user_2003361749501260298@testuser.com", //orden.correo(),
                orden.desarrolloId(),
                isSandbox,
                checkoutItems,
                redirectSuccess,
                redirectFailure,
                redirectCancel,
                expirationDate
        );

        // 4. Iniciar sesión de pago en el microservicio de checkout
        CheckoutInitResponse checkoutResponse = checkoutClient.iniciarSesionPago(checkoutRequest);

        // 5. Registrar el intento de pago en la base de datos en estatus PENDIENTE
        // Guardamos el uuid del checkout y los metadatos originales (los cuales contienen los datos de la reserva)
        Map<String, Object> metadataMap = new HashMap<>();
        if (request.metadata() != null && !request.metadata().isBlank()) {
            try {
                metadataMap.putAll(JsonUtils.fromJson(request.metadata(), Map.class));
            } catch (Exception e) {
                log.warn("No se pudo parsear metadatos originales en LinkPaymentStrategy: {}", request.metadata());
            }
        }
        metadataMap.put("checkoutUuid", checkoutResponse.uuid());
        metadataMap.put("checkoutUrl", checkoutResponse.checkoutUrl());
        String metadataStr = JsonUtils.toJson(metadataMap);

        Integer intentoId = intentoPagoRepository.spCobranzaRegistrarIntentoPago(
                ordenUuid,
                getGatewayType(),
                request.monto(),
                EstatusIntentoPago.PENDIENTE.toString(),
                metadataStr
        ).orElseThrow(() -> new IllegalStateException("No se pudo registrar el intento de pago para la orden " + ordenUuid));

        log.info("Intento de pago con LINK generado, orden {} registrado con ID {} y checkoutUuid {}",
                ordenUuid, intentoId, checkoutResponse.uuid());

        return ProcesarPagoResponse.builder()
                .intentoPagoId(intentoId)
                .estatus(EstatusIntentoPago.PENDIENTE.toString())
                .mensajeAccion("Link de pago generado con éxito.")
                .urlPago(checkoutResponse.checkoutUrl())
                .build();
    }

    @Override
    public void postProcesarFinalizacion(Integer idIntentoPago) {
        intentoPagoRepository.spCobranzaRegistrarPagoLink(idIntentoPago);
    }
}
