package com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.strategy;

import com.coralclubes.facil.modules.prospectos.dto.domain.EventoResultadoCita;
import com.coralclubes.facil.shared.infrastructure.integration.crm.model.CrmProvider;
import com.coralclubes.facil.shared.infrastructure.integration.crm.ports.CrmEventSenderStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Estrategia de emisión de eventos de citas para el proveedor Zoho CRM.
 * Envía el payload JSON hacia la función / webhook de Zoho CRM (ej. receiver_result execute).
 */
@Component
@Slf4j
public class ZohoCrmEventSenderStrategy implements CrmEventSenderStrategy {

    private final RestClient restClient;
    private final String webhookUrl;
    private final ObjectMapper objectMapper;

    public ZohoCrmEventSenderStrategy(
            @Value("${app.crm.zoho.webhook-url:}") String webhookUrl,
            ObjectMapper objectMapper
    ) {
        this.webhookUrl = webhookUrl;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public void emitirEvento(EventoResultadoCita evento) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("[ZOHO CRM SENDER] No se configuró 'app.crm.zoho.webhook-url'. Evento omitido para lead: {}", evento.idExterno());
            return;
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(evento);
            log.info("[ZOHO CRM SENDER] Enviando evento hacia Zoho Función/Webhook URL: {}\nPayload: {}",
                    webhookUrl, jsonPayload);

            String response = restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonPayload)
                    .retrieve()
                    .body(String.class);

            log.info("[ZOHO CRM SENDER] Respuesta exitosa de Zoho CRM: {}", response);
        } catch (Exception e) {
            log.error("[ZOHO CRM SENDER] Error al emitir evento hacia Zoho para lead {}: {}",
                    evento.idExterno(), e.getMessage(), e);
        }
    }

    @Override
    public CrmProvider getProveedor() {
        return CrmProvider.ZOHO;
    }
}
