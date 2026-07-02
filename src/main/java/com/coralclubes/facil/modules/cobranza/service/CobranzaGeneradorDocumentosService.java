package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.DatosEstadoCuentaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaLegacyDto;
import com.coralclubes.facil.shared.infrastructure.pdf.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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

    public byte[] generarPdfEstadoCuenta(DatosEstadoCuentaDto datos) {
        List<Map<String, Object>> movimientosFormateados = new ArrayList<>();
        if (datos.movimientos() != null) {
            for (var mov : datos.movimientos()) {
                Map<String, Object> m = new HashMap<>();
                m.put("fecha", mov.fecha() != null ? mov.fecha() : "");
                m.put("fechaVencimiento", mov.fechaVencimiento() != null ? mov.fechaVencimiento() : "");
                m.put("concepto", mov.concepto() != null ? mov.concepto() : "");
                m.put("montoCargo", mov.montoCargo() != null ? mov.montoCargo() : "");
                m.put("montoInteres", mov.montoInteres() != null ? mov.montoInteres() : "");
                m.put("montoPendiente", mov.montoPendiente() != null ? mov.montoPendiente() : "");
                movimientosFormateados.add(m);
            }
        }

        Map<String, Object> resumenMap = new HashMap<>();
        if (datos.resumenTotales() != null) {
            resumenMap.put("totalCargos", datos.resumenTotales().totalCargos() != null ? datos.resumenTotales().totalCargos() : "");
            resumenMap.put("totalIntereses", datos.resumenTotales().totalIntereses() != null ? datos.resumenTotales().totalIntereses() : "");
            resumenMap.put("totalNetoExigible", datos.resumenTotales().totalNetoExigible() != null ? datos.resumenTotales().totalNetoExigible() : "");
        }

        Map<String, Object> variables = Map.ofEntries(
                Map.entry("razonSocial", datos.razonSocial() != null ? datos.razonSocial() : ""),
                Map.entry("slogan", datos.slogan() != null ? datos.slogan() : ""),
                Map.entry("periodoInicio", datos.periodoInicio() != null ? datos.periodoInicio() : ""),
                Map.entry("periodoFin", datos.periodoFin() != null ? datos.periodoFin() : ""),
                Map.entry("fechaEmision", datos.fechaEmision() != null ? datos.fechaEmision() : ""),
                Map.entry("fechaLimitePago", datos.fechaLimitePago() != null ? datos.fechaLimitePago() : ""),
                Map.entry("titular", datos.titular() != null ? datos.titular() : ""),
                Map.entry("membresia", datos.membresia() != null ? datos.membresia() : ""),
                Map.entry("tipoMembresia", datos.tipoMembresia() != null ? datos.tipoMembresia() : ""),
                Map.entry("telefonoContacto", datos.telefonoContacto() != null ? datos.telefonoContacto() : ""),
                Map.entry("correoContacto", datos.correoContacto() != null ? datos.correoContacto() : ""),
                Map.entry("domicilioSocio", datos.domicilioSocio() != null ? datos.domicilioSocio() : ""),
                Map.entry("movimientos", movimientosFormateados),
                Map.entry("resumenTotales", resumenMap)
        );

        return pdfGenerator.generarPdfDesdeHtml("ESTADO_CUENTA", variables);
    }
}