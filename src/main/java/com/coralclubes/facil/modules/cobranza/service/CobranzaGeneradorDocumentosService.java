package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.DatosEstadoCuentaDto;
import com.coralclubes.facil.modules.cobranza.dto.response.DatosEstadoCuentaHistoricoDto;
import com.coralclubes.facil.modules.clientes.dto.response.DatosReporteBeneficiariosDto;
import com.coralclubes.facil.modules.clientes.dto.response.ConsumoPuntosPdfDto;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosLiberadosPdfDto;
import com.coralclubes.facil.modules.clientes.dto.response.EstadoCuentaPuntosPdfDto;
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

        String estatusDocumento = "CANCELACION".equalsIgnoreCase(tipo) ? "CANCELADO" : tipo;

        Map<String, Object> variables = Map.ofEntries(
                Map.entry("estatus", estatusDocumento), // 'ORIGINAL', 'REIMPRESIÓN' o 'CANCELADO'
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

        Map<String, Object> variables = new HashMap<>();
        variables.put("razonSocial", datos.razonSocial() != null ? datos.razonSocial() : "");
        variables.put("slogan", datos.slogan() != null ? datos.slogan() : "");
        variables.put("periodoInicio", datos.periodoInicio() != null ? datos.periodoInicio() : "");
        variables.put("periodoFin", datos.periodoFin() != null ? datos.periodoFin() : "");
        variables.put("fechaEmision", datos.fechaEmision() != null ? datos.fechaEmision() : "");
        variables.put("fechaLimitePago", datos.fechaLimitePago() != null ? datos.fechaLimitePago() : "");
        variables.put("titular", datos.titular() != null ? datos.titular() : "");
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "");
        variables.put("tipoMembresia", datos.tipoMembresia() != null ? datos.tipoMembresia() : "");
        variables.put("telefonoContacto", datos.telefonoContacto() != null ? datos.telefonoContacto() : "");
        variables.put("correoContacto", datos.correoContacto() != null ? datos.correoContacto() : "");
        variables.put("domicilioSocio", datos.domicilioSocio() != null ? datos.domicilioSocio() : "");
        variables.put("movimientos", movimientosFormateados);
        variables.put("resumenTotales", resumenMap);
        variables.put("puntosLiberados", datos.puntosLiberados());
        variables.put("puntosConsumidos", datos.puntosConsumidos());
        variables.put("puntosDisponibles", datos.puntosDisponibles());

        return pdfGenerator.generarPdfDesdeHtml("ESTADO_CUENTA", variables);
    }

    public byte[] generarPdfEstadoCuentaHistorico(DatosEstadoCuentaHistoricoDto datos) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("razonSocial", datos.razonSocial() != null ? datos.razonSocial() : "");
        variables.put("slogan", datos.slogan() != null ? datos.slogan() : "");
        variables.put("periodoInicio", datos.periodoInicio() != null ? datos.periodoInicio() : "");
        variables.put("periodoFin", datos.periodoFin() != null ? datos.periodoFin() : "");
        variables.put("fechaEmision", datos.fechaEmision() != null ? datos.fechaEmision() : "");
        variables.put("titular", datos.titular() != null ? datos.titular() : "");
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "");
        variables.put("tipoMembresia", datos.tipoMembresia() != null ? datos.tipoMembresia() : "");
        variables.put("telefonoContacto", datos.telefonoContacto() != null ? datos.telefonoContacto() : "");
        variables.put("correoContacto", datos.correoContacto() != null ? datos.correoContacto() : "");
        variables.put("domicilioSocio", datos.domicilioSocio() != null ? datos.domicilioSocio() : "");
        variables.put("puntosLiberados", datos.puntosLiberados());
        variables.put("puntosConsumidos", datos.puntosConsumidos());
        variables.put("puntosDisponibles", datos.puntosDisponibles());
        variables.put("movimientosHistoricos", datos.movimientosHistoricos() != null ? datos.movimientosHistoricos() : List.of());

        return pdfGenerator.generarPdfDesdeHtml("ESTADO_CUENTA_HISTORICO", variables);
    }

    public byte[] generarPdfReporteBeneficiarios(DatosReporteBeneficiariosDto datos) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("razonSocial", datos.razonSocial() != null ? datos.razonSocial() : "");
        variables.put("slogan", datos.slogan() != null ? datos.slogan() : "");
        variables.put("fechaEmision", datos.fechaEmision() != null ? datos.fechaEmision() : "");
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "");
        variables.put("clasificacionMembresia", datos.clasificacionMembresia() != null ? datos.clasificacionMembresia() : "");
        variables.put("tipoMembresia", datos.tipoMembresia() != null ? datos.tipoMembresia() : "");
        variables.put("desarrollo", datos.desarrollo() != null ? datos.desarrollo() : "");
        variables.put("direccionMembresia", datos.direccionMembresia() != null ? datos.direccionMembresia() : "");

        List<Map<String, Object>> list = new ArrayList<>();
        if (datos.beneficiarios() != null) {
            for (var b : datos.beneficiarios()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", b.id() != null ? b.id() : "");
                map.put("nombre", b.nombre() != null ? b.nombre() : "");
                map.put("fechaNacimiento", b.fechaNacimiento() != null ? b.fechaNacimiento() : "");
                map.put("estadoCivil", b.estadoCivil() != null ? b.estadoCivil() : "");
                map.put("parentesco", b.parentesco() != null ? b.parentesco() : "");
                list.add(map);
            }
        }
        variables.put("beneficiarios", list);

        return pdfGenerator.generarPdfDesdeHtml("REPORTE_BENEFICIARIOS", variables);
    }

    public byte[] generarPdfConsumoPuntos(ConsumoPuntosPdfDto datos) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("razonSocial", datos.razonSocial() != null ? datos.razonSocial() : "");
        variables.put("slogan", datos.slogan() != null ? datos.slogan() : "");
        variables.put("periodoInicio", datos.periodoInicio() != null ? datos.periodoInicio() : "");
        variables.put("periodoFin", datos.periodoFin() != null ? datos.periodoFin() : "");
        variables.put("fechaEmision", datos.fechaEmision() != null ? datos.fechaEmision() : "");
        variables.put("titular", datos.titular() != null ? datos.titular() : "");
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "");
        variables.put("tipoMembresia", datos.tipoMembresia() != null ? datos.tipoMembresia() : "");
        variables.put("desarrollo", datos.desarrollo() != null ? datos.desarrollo() : "");
        variables.put("clasificacionMembresia", datos.clasificacionMembresia() != null ? datos.clasificacionMembresia() : "");
        variables.put("domicilioSocio", datos.domicilioSocio() != null ? datos.domicilioSocio() : "");

        List<Map<String, Object>> consumosMapList = new ArrayList<>();
        if (datos.consumosPuntos() != null) {
            for (var c : datos.consumosPuntos()) {
                Map<String, Object> item = new HashMap<>();
                item.put("fechaConsumo", c.fechaConsumo() != null ? c.fechaConsumo() : "");
                item.put("accesoClub", c.accesoClub() != null ? c.accesoClub() : "");
                item.put("desarrolloUso", c.desarrolloUso() != null ? c.desarrolloUso() : "");
                item.put("tipoCliente", c.tipoCliente() != null ? c.tipoCliente() : "");
                item.put("zonaAcceso", c.zonaAcceso() != null ? c.zonaAcceso() : "");
                item.put("periodoAcceso", c.periodoAcceso() != null ? c.periodoAcceso() : "");
                item.put("puntosHospedaje", c.puntosHospedaje() != null ? c.puntosHospedaje() : "");
                item.put("puntosInstalaciones", c.puntosInstalaciones() != null ? c.puntosInstalaciones() : "");
                item.put("puntosCampoGolf", c.puntosCampoGolf() != null ? c.puntosCampoGolf() : "");
                item.put("numeroAutorizacion", c.numeroAutorizacion() != null ? c.numeroAutorizacion() : "");
                item.put("descripcionReferencia", c.descripcionReferencia() != null ? c.descripcionReferencia() : "");
                consumosMapList.add(item);
            }
        }
        variables.put("consumosPuntos", consumosMapList);

        return pdfGenerator.generarPdfDesdeHtml("CONSUMO_PUNTOS", variables);
    }

    public byte[] generarPdfPuntosLiberados(PuntosLiberadosPdfDto datos) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("razonSocial", datos.razonSocial() != null ? datos.razonSocial() : "");
        variables.put("slogan", datos.slogan() != null ? datos.slogan() : "");
        variables.put("periodoInicio", datos.periodoInicio() != null ? datos.periodoInicio() : "");
        variables.put("periodoFin", datos.periodoFin() != null ? datos.periodoFin() : "");
        variables.put("fechaEmision", datos.fechaEmision() != null ? datos.fechaEmision() : "");
        variables.put("titular", datos.titular() != null ? datos.titular() : "");
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "");
        variables.put("tipoMembresia", datos.tipoMembresia() != null ? datos.tipoMembresia() : "");
        variables.put("desarrollo", datos.desarrollo() != null ? datos.desarrollo() : "");
        variables.put("clasificacionMembresia", datos.clasificacionMembresia() != null ? datos.clasificacionMembresia() : "");
        variables.put("domicilioSocio", datos.domicilioSocio() != null ? datos.domicilioSocio() : "");

        List<Map<String, Object>> liberadosMapList = new ArrayList<>();
        if (datos.puntosLiberados() != null) {
            for (var l : datos.puntosLiberados()) {
                Map<String, Object> item = new HashMap<>();
                item.put("numeroPlan", l.numeroPlan() != null ? l.numeroPlan() : "");
                item.put("fechaLiberacion", l.fechaLiberacion() != null ? l.fechaLiberacion() : "");
                item.put("concepto", l.concepto() != null ? l.concepto() : "");
                item.put("puntos", l.puntos() != null ? l.puntos() : "");
                item.put("recibo", l.recibo() != null ? l.recibo() : "");
                liberadosMapList.add(item);
            }
        }
        variables.put("puntosLiberados", liberadosMapList);

        Map<String, Object> resumenMap = new HashMap<>();
        if (datos.resumenPuntos() != null) {
            resumenMap.put("puntosLiberadosPeriodo", datos.resumenPuntos().puntosLiberadosPeriodo() != null ? datos.resumenPuntos().puntosLiberadosPeriodo() : "");
            resumenMap.put("puntosLiberadosPrevios", datos.resumenPuntos().puntosLiberadosPrevios() != null ? datos.resumenPuntos().puntosLiberadosPrevios() : "");
            resumenMap.put("totalPuntosLiberados", datos.resumenPuntos().totalPuntosLiberados() != null ? datos.resumenPuntos().totalPuntosLiberados() : "");
        }
        variables.put("resumenPuntos", resumenMap);

        return pdfGenerator.generarPdfDesdeHtml("PUNTOS_LIBERADOS", variables);
    }

    public byte[] generarPdfEstadoCuentaPuntos(EstadoCuentaPuntosPdfDto datos) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("razonSocial", datos.razonSocial() != null ? datos.razonSocial() : "");
        variables.put("slogan", datos.slogan() != null ? datos.slogan() : "");
        variables.put("periodoInicio", datos.periodoInicio() != null ? datos.periodoInicio() : "");
        variables.put("periodoFin", datos.periodoFin() != null ? datos.periodoFin() : "");
        variables.put("fechaEmision", datos.fechaEmision() != null ? datos.fechaEmision() : "");
        variables.put("titular", datos.titular() != null ? datos.titular() : "");
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "");
        variables.put("tipoMembresia", datos.tipoMembresia() != null ? datos.tipoMembresia() : "");
        variables.put("desarrollo", datos.desarrollo() != null ? datos.desarrollo() : "");
        variables.put("clasificacionMembresia", datos.clasificacionMembresia() != null ? datos.clasificacionMembresia() : "");
        variables.put("domicilioSocio", datos.domicilioSocio() != null ? datos.domicilioSocio() : "");

        List<Map<String, Object>> movsMapList = new ArrayList<>();
        if (datos.movimientosGeneralPuntos() != null) {
            for (var m : datos.movimientosGeneralPuntos()) {
                Map<String, Object> item = new HashMap<>();
                item.put("planVenta", m.planVenta() != null ? m.planVenta() : "");
                item.put("inicioVigencia", m.inicioVigencia() != null ? m.inicioVigencia() : "");
                item.put("finVigencia", m.finVigencia() != null ? m.finVigencia() : "");
                item.put("puntosMembresia", m.puntosMembresia() != null ? m.puntosMembresia() : "");
                item.put("puntosEnganche", m.puntosEnganche() != null ? m.puntosEnganche() : "");
                item.put("puntosMensualidades", m.puntosMensualidades() != null ? m.puntosMensualidades() : "");
                item.put("descripcionMovimiento", m.descripcionMovimiento() != null ? m.descripcionMovimiento() : "");
                item.put("puntosLiberados", m.puntosLiberados() != null ? m.puntosLiberados() : "");
                item.put("puntosConsumidos", m.puntosConsumidos() != null ? m.puntosConsumidos() : "");
                item.put("puntosHospedaje", m.puntosHospedaje() != null ? m.puntosHospedaje() : "");
                item.put("puntosInstalaciones", m.puntosInstalaciones() != null ? m.puntosInstalaciones() : "");
                item.put("puntosGolf", m.puntosGolf() != null ? m.puntosGolf() : "");
                item.put("saldoPuntosLibres", m.saldoPuntosLibres() != null ? m.saldoPuntosLibres() : "");
                item.put("estatusPuntos", m.estatusPuntos() != null ? m.estatusPuntos() : "");
                movsMapList.add(item);
            }
        }
        variables.put("movimientosGeneralPuntos", movsMapList);

        Map<String, Object> totalesMap = new HashMap<>();
        if (datos.totalesGenerales() != null) {
            totalesMap.put("totalPuntosMembresia", datos.totalesGenerales().totalPuntosMembresia() != null ? datos.totalesGenerales().totalPuntosMembresia() : "");
            totalesMap.put("totalPuntosEnganche", datos.totalesGenerales().totalPuntosEnganche() != null ? datos.totalesGenerales().totalPuntosEnganche() : "");
            totalesMap.put("totalPuntosLiberados", datos.totalesGenerales().totalPuntosLiberados() != null ? datos.totalesGenerales().totalPuntosLiberados() : "");
            totalesMap.put("totalPuntosConsumidos", datos.totalesGenerales().totalPuntosConsumidos() != null ? datos.totalesGenerales().totalPuntosConsumidos() : "");
            totalesMap.put("totalPuntosHospedaje", datos.totalesGenerales().totalPuntosHospedaje() != null ? datos.totalesGenerales().totalPuntosHospedaje() : "");
            totalesMap.put("totalPuntosInstalaciones", datos.totalesGenerales().totalPuntosInstalaciones() != null ? datos.totalesGenerales().totalPuntosInstalaciones() : "");
            totalesMap.put("totalPuntosGolf", datos.totalesGenerales().totalPuntosGolf() != null ? datos.totalesGenerales().totalPuntosGolf() : "");
            totalesMap.put("saldoPuntosLibres", datos.totalesGenerales().saldoPuntosLibres() != null ? datos.totalesGenerales().saldoPuntosLibres() : "");
        }
        variables.put("totalesGenerales", totalesMap);

        return pdfGenerator.generarPdfDesdeHtml("ESTADO_CUENTA_PUNTOS", variables);
    }
}