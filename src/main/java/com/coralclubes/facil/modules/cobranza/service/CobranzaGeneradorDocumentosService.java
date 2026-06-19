package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaLegacyDto;
import com.coralclubes.facil.shared.infrastructure.pdf.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Year;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CobranzaGeneradorDocumentosService {

    private final PdfGeneratorService pdfGenerator;
    private final StorageClient storageClient;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasConfiguracion;

    public byte[] generarPdfRecibo(DatosReciboResponse recibo, String tipo, String cadenaSeguridad) {
        // Formatear decimales en Java
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        
        List<Map<String, Object>> movimientosFormateados = new ArrayList<>();
        if (recibo.getMovimientos() != null) {
            for (var mov : recibo.getMovimientos()) {
                Map<String, Object> m = new HashMap<>();
                m.put("descripcion", mov.getDescripcion());
                m.put("referencia", mov.getReferencia());
                m.put("importe", df.format(mov.getImporte()));
                m.put("interes", df.format(mov.getInteres()));
                m.put("descuento", mov.getDescuento().compareTo(BigDecimal.ZERO) > 0 ? "-" + df.format(mov.getDescuento()) : "$0.00");
                m.put("totalNeto", df.format(mov.getTotalNeto()));
                movimientosFormateados.add(m);
            }
        }

        Map<String, Object> variables = Map.ofEntries(
                Map.entry("estatus", tipo), // 'ORIGINAL', 'REIMPRESIÓN' o 'CANCELADO'
                // Datos de la Empresa
                Map.entry("empresa", recibo.getEmpresa() != null ? recibo.getEmpresa() : ""),
                Map.entry("rfcEmpresa", recibo.getRfcEmpresa() != null ? recibo.getRfcEmpresa() : ""),
                Map.entry("direccionEmpresa", recibo.getDireccionEmpresa() != null ? recibo.getDireccionEmpresa() : ""),
                Map.entry("telefonoEmpresa", recibo.getTelefonoEmpresa() != null ? recibo.getTelefonoEmpresa() : ""),
                Map.entry("webEmpresa", recibo.getWebEmpresa() != null ? recibo.getWebEmpresa() : ""),
                Map.entry("correoEmpresa", recibo.getCorreoEmpresa() != null ? recibo.getCorreoEmpresa() : ""),
                // Metadatos del Recibo
                Map.entry("folio", recibo.getFolio() != null ? recibo.getFolio() : ""),
                Map.entry("fecha", recibo.getFecha() != null ? recibo.getFecha() : ""),
                // Información del Socio y Producto
                Map.entry("clienteNombre", recibo.getClienteNombre() != null ? recibo.getClienteNombre() : ""),
                Map.entry("membresia", recibo.getMembresia() != null ? recibo.getMembresia() : ""),
                Map.entry("direccionSocio", recibo.getDireccionSocio() != null ? recibo.getDireccionSocio() : ""),
                Map.entry("desarrollo", recibo.getDesarrollo() != null ? recibo.getDesarrollo() : ""),
                Map.entry("producto", recibo.getProducto() != null ? recibo.getProducto() : ""),
                // Desglose Financiero y Tabla de Movimientos
                Map.entry("movimientos", movimientosFormateados),
                Map.entry("subtotal", df.format(recibo.getSubtotal())),
                Map.entry("totalIva", df.format(recibo.getTotalIva())),
                Map.entry("descuentoTotal", recibo.getDescuentoTotal().compareTo(BigDecimal.ZERO) > 0 ? "-" + df.format(recibo.getDescuentoTotal()) : "$0.00"),
                Map.entry("total", df.format(recibo.getTotal())),
                // Seguridad Digital
                Map.entry("cadenaSeguridad", cadenaSeguridad)
        );

        // Generamos el PDF localmente en memoria (Pebble + Gotenberg)
        return pdfGenerator.generarPdfDesdeHtml("RECIBO_FACIL", variables);
    }

    public UUID generarYCargarPdfRecibo(DatosReciboResponse recibo, String tipo, String cadenaSeguridad) {
        byte[] file = generarPdfRecibo(recibo, tipo, cadenaSeguridad);
        return cargarPdf(file, tipo, recibo.getFolio(), recibo.getMembresia()).uuid();
    }

    private InfoArchivoDto cargarPdf (byte[] file, String tipo, String folio, String membresia) {
        // Subimos el PDF al Storage
        String nombreArchivo = tipo + "_RECIBO_" + folio + "_" + System.currentTimeMillis() + ".pdf";

        SolicitudCargaLegacyDto solicitud = SolicitudCargaLegacyDto.builder()
                .requiereDepuracion(false)
                .esPublico(false)
                .aliasConfiguracion(aliasConfiguracion)
                .rutaLogica("cobranza/recibos/" + membresia + "/" + folio)
                .metadatos(Map.of(
                        "folio", folio,
                        "subidoPor", "SYSTEM",
                        "modulo", "RECIBOS"
                ))
                .build();

        return storageClient.cargarArchivoSincrono(file, nombreArchivo,"application/pdf", solicitud);
    }
}