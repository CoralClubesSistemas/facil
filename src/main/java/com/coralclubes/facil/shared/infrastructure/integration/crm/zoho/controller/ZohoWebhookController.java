package com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.controller;

import com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.adapter.ZohoProspectoAdapter;
import com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.dto.ZohoWebhookPayload;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para la recepción de webhooks y notificaciones desde Zoho CRM.
 */
@RestController
@RequestMapping("/api/v1/public/zoho/webhook")
@RequiredArgsConstructor
@Slf4j
public class ZohoWebhookController {

    private final ZohoProspectoAdapter zohoProspectoAdapter;

    /**
     * Endpoint para recibir webhooks de Zoho CRM en formato JSON.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> recibirWebhookJson(@RequestBody ZohoWebhookPayload payload) {
        log.info("[ZOHO WEBHOOK] Se recibió evento de cita desde Zoho para Lead ID: {}", payload.getEffectiveLeadId());
        zohoProspectoAdapter.procesarWebhookZoho(payload, null);
        return ResponseEntity.ok(ApiResponse.success("Evento de Zoho procesado exitosamente", payload.getEffectiveLeadId()));
    }

    /**
     * Endpoint para recibir webhooks de Zoho CRM en formato Form-UrlEncoded.
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<String>> recibirWebhookFormUrlEncoded(@RequestParam Map<String, String> params) {
        String leadId = params.getOrDefault("entity_id", params.getOrDefault("id", "UNKNOWN_LEAD_ID"));
        log.info("[ZOHO WEBHOOK] Se recibió evento de cita desde Zoho para Lead ID: {}", leadId);
        zohoProspectoAdapter.procesarWebhookZoho(null, params);
        return ResponseEntity.ok(ApiResponse.success("Evento de Zoho procesado exitosamente", leadId));
    }
}
