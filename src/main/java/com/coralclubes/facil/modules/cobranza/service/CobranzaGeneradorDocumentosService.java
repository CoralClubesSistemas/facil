package com.coralclubes.facil.modules.cobranza.service;


import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator.PdfGeneratorClient;
import com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator.dto.GeneratePdfRequest;
import com.coralclubes.facil.shared.infrastructure.integration.pdfgenerator.dto.StorageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CobranzaGeneradorDocumentosService {
    private final PdfGeneratorClient pdfGenerator;

    @Value("${app.clients.storage.api-key}")
    private String storageApiKey;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasConfiguracion;

    public record ResultadoRecibos(UUID originalId, UUID reimpresionId, String cadenaOriginal) {
    }

    public ResultadoRecibos generarAmbosRecibos(DatosReciboResponse recibo) {
        // 1. Generamos la Cadena de Seguridad del Original
        String cadenaOriginal = generarCadenaSeguridad(recibo, "ORIGINAL");
        UUID originalId = generarReciboEspecifico(recibo, "ORIGINAL", cadenaOriginal);

        // 2. Generamos la de Reimpresión
        String cadenaReimpresion = generarCadenaSeguridad(recibo, "REIMPRESION");
        UUID reimpresionId = generarReciboEspecifico(recibo, "REIMPRESION", cadenaReimpresion);

        return new ResultadoRecibos(originalId, reimpresionId, cadenaOriginal);
    }

    public UUID generarReciboCancelacion(DatosReciboResponse recibo) {
        String cadenaCancelacion = generarCadenaSeguridad(recibo, "CANCELADO");
        return generarReciboEspecifico(recibo, "CANCELADO", cadenaCancelacion);
    }

    // Modificación en CobranzaGeneradorDocumentosService.java
    private UUID generarReciboEspecifico(DatosReciboResponse recibo, String tipo, String cadenaSeguridad) {
        Map<String, Object> variables = Map.ofEntries(
                Map.entry("estatus", tipo), // 'ORIGINAL', 'REIMPRESIÓN' o 'CANCELADO'

                // Datos de la EmpresaMap.entry("empresa", recibo.getEmpresa()),
                Map.entry("rfcEmpresa", recibo.getRfcEmpresa()),
                Map.entry("direccionEmpresa", recibo.getDireccionEmpresa()),
                Map.entry("telefonoEmpresa", recibo.getTelefonoEmpresa()),
                Map.entry("webEmpresa", recibo.getWebEmpresa()),
                Map.entry("correoEmpresa", recibo.getCorreoEmpresa()),

                // Metadatos del Recibo
                Map.entry("folio", recibo.getFolio()),
                Map.entry("fecha", recibo.getFecha()),

                // Información del Socio y Producto
                Map.entry("clienteNombre", recibo.getClienteNombre()),
                Map.entry("membresia", recibo.getMembresia()),
                Map.entry("direccionSocio", recibo.getDireccionSocio()),
                Map.entry("desarrollo", recibo.getDesarrollo()),
                Map.entry("producto", recibo.getProducto()),

                // Desglose Financiero y Tabla de Movimientos
                Map.entry("movimientos", recibo.getMovimientos()),
                Map.entry("subtotal", recibo.getSubtotal()),
                Map.entry("totalIva", recibo.getTotalIva()),
                Map.entry("descuentoTotal", recibo.getDescuentoTotal()),
                Map.entry("total", recibo.getTotal()),

                // Seguridad Digital
                Map.entry("cadenaSeguridad", cadenaSeguridad)
        );

        GeneratePdfRequest request = GeneratePdfRequest.builder()
                .templateCode("RECIBO_FACIL")
                .data(variables)
                .storageConfig(StorageConfig.builder()
                        .xApiKeyStorage(storageApiKey)
                        .aliasConfiguracion(aliasConfiguracion)
                        .rutaLogica("cobranza/recibos/" + Year.now().getValue() + "/" + recibo.getDesarrollo() + "/" + recibo.getFolio())
                        // Nombre de archivo descriptivo
                        .nombreArchivo(tipo + "_RECIBO_" + recibo.getFolio() + "_" + System.currentTimeMillis() + ".pdf")
                        .build())
                .build();

        String fileId = pdfGenerator.generarYSubir(request);
        return UUID.fromString(fileId);
    }

    public String generarCadenaSeguridad(DatosReciboResponse recibo, String tipo) {
        String cadenaOriginal = String.format("||%s|%s|%s|%s|%s||",
                tipo.toUpperCase(),
                recibo.getFolio(),
                recibo.getFecha(),
                recibo.getMembresia(),
                recibo.getTotal()
        );

        String hashSignatura = DigestUtils.sha256Hex(cadenaOriginal).substring(0, 12);
        return cadenaOriginal + hashSignatura.toUpperCase();
    }
}