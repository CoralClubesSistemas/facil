package com.coralclubes.facil.shared.infrastructure.integration.zoho.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Payload de evento recibido desde Zoho CRM cuando cambia el estado de un prospecto (Lead).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ZohoLeadEventPayload(
        @JsonProperty("id") String id,
        @JsonProperty("entity_id") String entityId,
        @JsonProperty("module") String module,
        @JsonProperty("operation") String operation,
        @JsonProperty("lead_status") String leadStatus,
        @JsonProperty("status") String status,
        @JsonProperty("channel_id") String channelId,
        @JsonProperty("token") String token,
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
        return "UNKNOWN_LEAD_ID";
    }

    public String getEffectiveStatus() {
        if (leadStatus != null && !leadStatus.isBlank()) {
            return leadStatus;
        }
        if (status != null && !status.isBlank()) {
            return status;
        }
        if (data != null && data.containsKey("Lead_Status")) {
            return String.valueOf(data.get("Lead_Status"));
        }
        return "UNKNOWN_STATUS";
    }
}
