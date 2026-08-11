package com.coralclubes.facil.shared.infrastructure.integration.zoho.service;

import com.coralclubes.facil.shared.infrastructure.integration.zoho.dto.ZohoLeadEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio para procesar eventos y webhooks recibidos desde Zoho CRM.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ZohoWebhookService {

    /**
     * Procesa el evento de cambio de estatus de un Lead recibido de Zoho CRM.
     *
     * @param payload Payload deserializado o datos mapeados del webhook de Zoho.
     * @param rawParams Parámetros raw recibidos en el request (útil cuando Zoho envía form-urlencoded).
     */
    public void procesarEventoLead(ZohoLeadEventPayload payload, Map<String, String> rawParams) {
        String leadId = payload != null ? payload.getEffectiveLeadId() : rawParams.getOrDefault("entity_id", rawParams.get("id"));
        String status = payload != null ? payload.getEffectiveStatus() : rawParams.getOrDefault("lead_status", rawParams.get("status"));
        String module = payload != null && payload.module() != null ? payload.module() : rawParams.getOrDefault("module", "Leads");
        String operation = payload != null && payload.operation() != null ? payload.operation() : rawParams.getOrDefault("operation", "update");

        log.info("[ZOHO WEBHOOK EVENT] Recibido evento de Zoho CRM - Modulo: {}, Operacion: {}, LeadId: {}, Estatus: {}",
                module, operation, leadId, status);

        if (payload != null && payload.data() != null && !payload.data().isEmpty()) {
            log.debug("[ZOHO WEBHOOK EVENT DATA] Detalle del payload: {}", payload.data());
        } else if (rawParams != null && !rawParams.isEmpty()) {
            log.debug("[ZOHO WEBHOOK RAW PARAMS] Detalle de parametros raw: {}", rawParams);
        }

        // TODO: En la siguiente fase, conectar con el módulo de reservaciones para generar la cita automáticamente.
        log.info("[ZOHO WEBHOOK EVENT] Evento procesado y logeado correctamente para el Lead ID: {}", leadId);
    }
}
