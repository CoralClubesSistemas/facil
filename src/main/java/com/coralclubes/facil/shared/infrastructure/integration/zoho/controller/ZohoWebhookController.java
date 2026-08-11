package com.coralclubes.facil.shared.infrastructure.integration.zoho.controller;

import com.coralclubes.responses.ApiResponse;
import com.coralclubes.facil.shared.infrastructure.integration.zoho.dto.ZohoLeadEventPayload;
import com.coralclubes.facil.shared.infrastructure.integration.zoho.service.ZohoWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de webhook para recibir notificaciones y eventos en tiempo real desde Zoho CRM.
 */
@RestController
@RequestMapping("/api/v1/public/zoho/webhook")
@RequiredArgsConstructor
public class ZohoWebhookController {

    private final ZohoWebhookService zohoWebhookService;

    /**
     * Consume eventos en formato JSON enviados por Zoho CRM cuando un Lead cambia de estatus.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> recibirEventoJson(@RequestBody ZohoLeadEventPayload payload) {
        zohoWebhookService.procesarEventoLead(payload, Map.of());
        return ResponseEntity.ok(ApiResponse.success("Evento de Zoho procesado correctamente", "SUCCESS"));
    }

    /**
     * Consume eventos en formato Form URL-Encoded o Query Parameters enviados por Zoho CRM.
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse<String>> recibirEventoForm(@RequestParam Map<String, String> params) {
        zohoWebhookService.procesarEventoLead(null, params);
        return ResponseEntity.ok(ApiResponse.success("Evento de Zoho procesado correctamente", "SUCCESS"));
    }
}
