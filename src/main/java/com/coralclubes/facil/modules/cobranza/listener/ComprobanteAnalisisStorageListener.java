package com.coralclubes.facil.modules.cobranza.listener;

import com.coralclubes.facil.modules.cobranza.dto.response.AnalizarComprobanteResponse;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.shared.events.dto.StorageFileProcessedEvent;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Escucha eventos de archivos procesados en almacenamiento.
 * Si el archivo contiene el metadato 'tipoProceso = ANALISIS_COMPROBANTE',
 * lo procesa de forma inmediata sin esperar llamada del frontend e imprime el resultado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ComprobanteAnalisisStorageListener {

    private final CobranzaService cobranzaService;
    private final BusinessLogger businessLogger;

    @EventListener
    public void onStorageFileProcessed(StorageFileProcessedEvent event) {
        String tipoProceso = event.getMetadataValue("tipoProceso");
        if (!"ANALISIS_COMPROBANTE".equalsIgnoreCase(tipoProceso)) {
            return;
        }

        String usuario = event.getMetadataValue("subidoPor");
        if (usuario == null || usuario.isBlank()) {
            usuario = "SYSTEM";
        }

        log.info("[COBRANZA_ANALISIS] Evento detectado para archivo {} (tipoProceso: {}). Iniciando análisis inmediato.",
                event.fileId(), tipoProceso);

        try {
            // Procesamiento inmediato del comprobante sin triangular por frontend
            AnalizarComprobanteResponse resultado = cobranzaService.analizarComprobanteDeposito(event.fileId(), usuario);

            // Imprimir en consola / pantalla el resultado extraído
            System.out.println("================================================================================");
            System.out.println(">>> [COBRANZA] RESULTADO DEL ANÁLISIS DEL COMPROBANTE (INMEDIATO) <<<");
            System.out.println("Archivo UUID   : " + event.fileId());
            System.out.println("Motor Utilizado: " + resultado.motorUsado());
            System.out.println("Datos Extraídos: " + resultado.comprobante());
            if (resultado.mensaje() != null) {
                System.out.println("Advertencias   : " + resultado.mensaje());
            }
            System.out.println("================================================================================");

            businessLogger.info(usuario, "Análisis inmediato completado para comprobante {}. Motor: {}, Monto: {}, Rastreo: {}",
                    event.fileId(),
                    resultado.motorUsado(),
                    resultado.comprobante() != null ? resultado.comprobante().monto() : null,
                    resultado.comprobante() != null ? resultado.comprobante().claveRastreo() : null
            );

        } catch (Exception e) {
            log.error("[COBRANZA_ANALISIS] Error al procesar inmediatamente el archivo " + event.fileId(), e);
        }
    }
}
