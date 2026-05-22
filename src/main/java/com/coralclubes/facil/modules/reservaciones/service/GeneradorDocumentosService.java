package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.DatosCartaOcupacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.utils.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeneradorDocumentosService {

    private final StorageClient storageClient;
    private final PdfGeneratorService pdfGenerator;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorage;

    /**
     * Genera la Carta de Ocupación en PDF, retorna la URL pública del archivo.
     */
    public UUID generarYGuardarCartaOcupacion(DatosCartaOcupacionDto datos) {

        // =========================================================================
        // 1. GENERACIÓN DEL PDF EN MEMORIA (Thymeleaf + OpenHTML)
        // =========================================================================
        Map<String, Object> variables = new HashMap<>();
        variables.put("fechaEmision", datos.fechaEmision());
        variables.put("titular", datos.titular());
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "PÚBLICO GENERAL");
        variables.put("foliosReservacion", datos.foliosReservacion());
        variables.put("habitaciones", datos.habitaciones());
        variables.put("observaciones", datos.observaciones() != null ? datos.observaciones() : "Sin observaciones adicionales.");
        variables.put("importeTotal", datos.importeTotal());
        variables.put("fechaEntrada", datos.fechaEntrada());
        variables.put("fechaSalida", datos.fechaSalida());
        variables.put("desarrollo", datos.desarrollo());

        // Convierte el HTML a bytes
        byte[] pdfBytes = pdfGenerator.generarPdfDesdeHtml("CARTA_OCUPACION", variables);

        // Limpiamos los folios para que el nombre del archivo no tenga espacios (Ej: CARTA_OCUPACION_1500_1501.pdf)
        String foliosLimpio = datos.foliosReservacion().replace(" ", "").replace(",", "_");
        String nombreArchivo = "CARTA_OCUPACION_" + foliosLimpio + ".pdf";

        // =========================================================================
        // Pedir permiso al MS Storage
        // =========================================================================
        SolicitudCargaDto solicitud = SolicitudCargaDto.builder()
                .requiereDepuracion(false)
                .nombreArchivo(nombreArchivo)
                .contentType("application/pdf")
                .tamanoBytes((long) pdfBytes.length)
                .esPublico(false)
                .aliasConfiguracion(aliasStorage)
                .rutaLogica("reservaciones/cartas-ocupacion/" + Year.now().getValue())
                .metadatos(Map.of("folios", datos.foliosReservacion()))
                .build();

        RespuestaCargaDto handshake = storageClient.solicitarUrlCarga(solicitud);

        // =========================================================================
        // CARGA DIRECTA
        // =========================================================================
        storageClient.subirArchivoBinario(handshake.uploadUrl(), pdfBytes, "application/pdf");

        // Damos una pequeña pausa (1 segundo) para permitir que el Storage Service
        // procese el Webhook de RabbitMQ que cambia el estado a 'READY'
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // =========================================================================
        // OBTENER URL PÚBLICA
        // =========================================================================
        return handshake.fileId();
    }
}