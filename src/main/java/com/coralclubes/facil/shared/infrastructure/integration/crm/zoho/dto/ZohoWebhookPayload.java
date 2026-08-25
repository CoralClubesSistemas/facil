package com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Payload de evento recibido desde Zoho CRM con mapeo específico del proveedor.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ZohoWebhookPayload(
        @JsonProperty("id") String id,
        @JsonProperty("entity_id") String entityId,
        Map<String, Object> data
) {
    public String getEffectiveLeadId() {
        if (entityId != null && !entityId.isBlank()) {
            return entityId;
        }
        if (id != null && !id.isBlank()) {
            return id;
        }
        if (data != null && data.containsKey("id")) {
            return String.valueOf(data.get("id"));
        }
        return null;
    }
}
