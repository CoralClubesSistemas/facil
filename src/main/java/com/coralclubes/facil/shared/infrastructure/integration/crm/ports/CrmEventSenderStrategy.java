package com.coralclubes.facil.shared.infrastructure.integration.crm.ports;

import com.coralclubes.facil.modules.prospectos.dto.domain.EventoResultadoCita;
import com.coralclubes.facil.shared.infrastructure.integration.crm.model.CrmProvider;

/**
 * Estrategia de envío de eventos hacia un CRM específico.
 * Cada proveedor encapsula internamente su mecanismo de emisión (Webhook, SDK, API REST, etc.).
 */
public interface CrmEventSenderStrategy {

    /**
     * Emite el evento hacia el CRM destino.
     */
    void emitirEvento(EventoResultadoCita evento);

    /**
     * Proveedor al que pertenece esta estrategia.
     */
    CrmProvider getProveedor();
}
