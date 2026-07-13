package com.coralclubes.facil.modules.cobranza.model.pagos.strategies;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.ProcesarPagoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.ConsultarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.model.pagos.enums.EstatusIntentoPago;
import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.CheckoutClient;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.dto.CheckoutInitRequest;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.dto.CheckoutInitResponse;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkPaymentStrategy implements PaymentStrategy {

    private final IntentoPagoRepository intentoPagoRepository;
    private final CobranzaRepository cobranzaRepository;
    private final CheckoutClient checkoutClient;
    private final ObjectMapper objectMapper;

    private final NotificationClient notificationClient;

    @Value("${app.clients.notifications.aliases.default}")
    private String aliasConfig;

    @Value("${app.clients.checkout.is-sandbox}")
    private boolean isSandbox;

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

        /* 2. Si el monto a pagar de la orden es igual al monto a pagar en esta forma de pago se mapean los
         * Items de la orden como items para el checkout, si es menor el monto den esta forma de pago
         * Se mapea un solo item con el monto a pagar de esta forma de pago y se ignoran los items de la orden */
        List<CheckoutInitRequest.CheckoutItem> checkoutItems = new ArrayList<>();

        if (request.monto().compareTo(orden.totalPagar()) < 0) {
            checkoutItems.add(new CheckoutInitRequest.CheckoutItem(
                    1,
                    "Pago parcial de la orden de cobranza",
                    1,
                    request.monto(),
                    "Pago parcial de orden de cobranza " + orden.numeroOrden()
            ));
        } else {
            checkoutItems = orden.conceptos().stream()
                    .map(c -> new CheckoutInitRequest.CheckoutItem(
                            c.idMovimiento(),
                            c.detalle(),
                            1,
                            c.subtotal(),
                            c.concepto()
                    ))
                    .toList();
        }

        // 3. Extraemos los metadatos de redirección y sandbox desde el request
        String redirectSuccess = this.getMetadataField(request.metadata(), "redirectSuccess");
        String redirectFailure = this.getMetadataField(request.metadata(), "redirectFailure");
        String redirectCancel = this.getMetadataField(request.metadata(), "redirectCancel");

        // Generamos la fecha de expiración del link de pago (1 día a partir de ahora)
        String expirationDate = LocalDateTime.now().plusDays(1).toString();

        String descripcion = "Pago de orden de cobranza " + orden.numeroOrden();

        CheckoutInitRequest checkoutRequest = CheckoutInitRequest.builder()
                .externalReference(ordenUuid.toString())
                .amount(request.monto())
                .description(descripcion)
                .payerEmail(isSandbox ? "test_user_2003361749501260298@testuser.com" : orden.correo())
                .desarrollo(orden.desarrolloId())
                .isSandbox(isSandbox)
                .items(checkoutItems)
                .urlRedirectOnSuccess(redirectSuccess)
                .urlRedirectOnFailure(redirectFailure)
                .urlRedirectOnCancel(redirectCancel)
                .expirationDate(expirationDate)
                .build();

        // 4. Iniciar sesión de pago en el microservicio de checkout
        CheckoutInitResponse checkoutResponse = checkoutClient.iniciarSesionPago(checkoutRequest);

        // 5. Registrar el intento de pago en la base de datos en estatus PENDIENTE
        // Guardamos el uuid del checkout y los metadatos originales
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("checkoutUuid", checkoutResponse.uuid());
        metadataMap.put("checkoutUrl", checkoutResponse.checkoutUrl());
        metadataMap.put("redirectSuccess", redirectSuccess);
        metadataMap.put("redirectFailure", redirectFailure);
        metadataMap.put("redirectCancel", redirectCancel);
        String metadataStr = JsonUtils.toJson(metadataMap);

        Integer intentoId = intentoPagoRepository.spCobranzaRegistrarIntentoPago(
                ordenUuid,
                getGatewayType(),
                request.monto(),
                EstatusIntentoPago.PENDIENTE.toString(),
                metadataStr
        ).orElseThrow(() -> new IllegalStateException("No se pudo registrar el intento de pago para la orden " + ordenUuid));

        // 6. Enviar el link de pago al correo del usuario si se proporcionó un correo en los metadatos
        String correoUsuario = this.getMetadataField(request.metadata(), "correoUsuario");
        if (correoUsuario != null && !correoUsuario.isBlank()) {
            this.sendLinkToUser(correoUsuario, checkoutResponse.checkoutUrl(), orden.nombreSocio(), descripcion, request.monto(), checkoutResponse.checkoutUrl());
        }

        log.info("Intento de pago con LINK generado, orden {} registrado con ID {} y checkoutUuid {}",
                ordenUuid, intentoId, checkoutResponse.uuid());

        // Insertamos el link de pago en el campo de datos adicionales
        Map<String, Object> datosAdicionales = new HashMap<>();
        datosAdicionales.put("checkoutUrl", checkoutResponse.checkoutUrl());

        return ProcesarPagoResponse.builder()
                .intentoPagoId(intentoId)
                .estatus(EstatusIntentoPago.PENDIENTE.toString())
                .mensajeAccion("Link de pago generado con éxito.")
                .datosAdicionales(datosAdicionales)
                .build();
    }

    @Override
    public void postProcesarFinalizacion(Integer idIntentoPago) {
        intentoPagoRepository.spCobranzaRegistrarPagoLink(idIntentoPago);
    }

    @Override
    public void eliminarIntento(UUID ordenUuid, Integer intentoPagoId, IntentoPagoDto intento) {
        try {
            if (intento.metadata() != null) {
                Map<String, Object> map = objectMapper.readValue(intento.metadata(), Map.class);

                if (map != null && map.get("checkoutUuid") != null) {
                    checkoutClient.cancelarSesionPago(map.get("checkoutUuid").toString());
                }
            }

            intentoPagoRepository.spCobranzaEliminarIntentoPago(ordenUuid, intentoPagoId);

        } catch (Exception e) {
            log.error(
                    "Error al eliminar el intento de pago {}: {}",
                    intentoPagoId,
                    e.getMessage(),
                    e
            );

            throw new RuntimeException("No fue posible cancelar la sesión de pago en Checkout.", e);
        }
    }

    private String getMetadataField(Map<String, Object> metadata, String fieldName) {
        if (metadata != null && metadata.containsKey(fieldName)) {
            return metadata.get(fieldName).toString();
        }
        return null;
    }

    private void sendLinkToUser(String email, String checkoutUrl, String nombreCliente, String conceptoPago, BigDecimal montoTotal, String urlEnlacePago) {
        log.info("Enviando link de pago a {}: {}", email, checkoutUrl);

        Map<String, Object> variables = new HashMap<>();
        variables.put("nombreEmpresa", "CORAL CLUBES");
        variables.put("nombreCliente", nombreCliente);
        variables.put("conceptoPago", conceptoPago);
        variables.put("montoTotal", String.format("$%.2f", montoTotal));
        variables.put("urlEnlacePago", urlEnlacePago);
        variables.put("yearActual", LocalDateTime.now().getYear());

        // Construir el cuerpo de la solicitud
        SolicitudNotificacionDto solicitud = SolicitudNotificacionDto.builder()
                .aliasConfig(aliasConfig)
                .destinatarios(List.of(email))
                .codigoPlantilla("link-checkout-pago-v1")
                .variables(variables)
                .build();

        // Enviamos la notificación
        notificationClient.enviarNotificacion(solicitud);
    }
}
