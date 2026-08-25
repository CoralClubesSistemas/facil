package com.coralclubes.facil.shared.infrastructure.integration.crm.service;

import com.coralclubes.facil.modules.prospectos.dto.domain.EventoResultadoCita;
import com.coralclubes.facil.shared.infrastructure.integration.crm.model.CrmProvider;
import com.coralclubes.facil.shared.infrastructure.integration.crm.ports.CrmEventSenderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio orquestador agnóstico que implementa CrmEventPublisherPort y resuelve
 * la estrategia de envío según el proveedor configurado en app.crm.provider.
 */
@Service
@Slf4j
public class CrmEventPublisherService {

    private final Map<CrmProvider, CrmEventSenderStrategy> strategies;
    private final CrmProvider configuredProvider;

    public CrmEventPublisherService(
            List<CrmEventSenderStrategy> strategyList,
            @Value("${app.crm.provider:zoho}") String providerStr
    ) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(CrmEventSenderStrategy::getProveedor, Function.identity()));
        this.configuredProvider = parseProvider(providerStr);
        log.info("[CRM PUBLISHER SERVICE] Inicializado con Proveedor activo: {}", this.configuredProvider);
    }

    @Async
    public void publicarResultadoCita(EventoResultadoCita evento) {
        log.info("[CRM PUBLISHER SERVICE] Emitiendo evento de resultado de cita para Lead ID: {} ({}) hacia Proveedor: {}",
                evento.idExterno(), evento.resultado(), configuredProvider);

        CrmEventSenderStrategy strategy = strategies.get(configuredProvider);

        if (strategy != null) {
            strategy.emitirEvento(evento);
        } else {
            log.warn("[CRM PUBLISHER SERVICE] No se encontró una estrategia registrada para el Proveedor: {}. Evento omitido.",
                    configuredProvider);
        }
    }

    private CrmProvider parseProvider(String str) {
        if (str == null || str.isBlank()) return CrmProvider.ZOHO;
        try {
            return CrmProvider.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[CRM PUBLISHER SERVICE] Proveedor '{}' no reconocido. Usando ZOHO por defecto", str);
            return CrmProvider.ZOHO;
        }
    }
}
