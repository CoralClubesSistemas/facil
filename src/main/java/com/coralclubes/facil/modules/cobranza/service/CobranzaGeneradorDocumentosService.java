package com.coralclubes.facil.modules.cobranza.service;


import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.utils.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CobranzaGeneradorDocumentosService {
    private final StorageClient storageClient;
    private final PdfGeneratorService pdfGenerator;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorage;

    /**
     * Genera un recibo de pago, retorna la URL pública del archivo.
     */
    public UUID generarYGuardarRecibo(DatosReciboResponse recibo) {

        // =========================================================================
        // 1. GENERACIÓN DEL PDF EN MEMORIA (Thymeleaf + OpenHTML)
        // =========================================================================
        Map<String, Object> variables = Map.ofEntries(
                Map.entry("empresa", recibo.getEmpresa()),
                Map.entry("rfcEmpresa", recibo.getRfcEmpresa()),
                Map.entry("direccionEmpresa", recibo.getDireccionEmpresa()),
                Map.entry("telefonoEmpresa", recibo.getTelefonoEmpresa()),
                Map.entry("webEmpresa", recibo.getWebEmpresa()),
                Map.entry("folio", recibo.getFolio()),
                Map.entry("tipoDocumento", "ORIGINAL"),
                Map.entry("fecha", recibo.getFecha() != null ? recibo.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""),
                Map.entry("moneda", recibo.getMoneda()),
                Map.entry("pagina", "1 de 1"),
                Map.entry("clienteNombre", recibo.getClienteNombre()),
                Map.entry("membresia", recibo.getMembresia()),
                Map.entry("direccionCliente", recibo.getDireccionCliente()),
                Map.entry("movimientos", recibo.getMovimientos()),
                Map.entry("subtotal", recibo.getSubtotal()),
                Map.entry("descuentoTotal", recibo.getDescuentoTotal()),
                Map.entry("total", recibo.getTotal()),
                Map.entry("cadenaSeguridad", recibo.getCadenaSeguridad()));

        // Convierte el HTML a bytes
        byte[] pdfBytes = pdfGenerator.generarPdfDesdeHtml("RECIBO", variables);

        String nombreArchivo = "RECIBO_" + recibo.getFolio() + "_" + Year.now().getValue() + ".pdf";

        // =========================================================================
        // Pedir permiso al MS Storage
        // =========================================================================
        SolicitudCargaDto solicitud = SolicitudCargaDto.builder()
                .nombreArchivo(nombreArchivo)
                .contentType("application/pdf")
                .tamanoBytes((long) pdfBytes.length)
                .esPublico(false)
                .aliasConfiguracion(aliasStorage)
                .rutaLogica("cobranza/recibos/" + Year.now().getValue())
                .metadatos(Map.of("cadenaSeguridad", recibo.getCadenaSeguridad()))
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