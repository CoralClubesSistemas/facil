package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.request.ConsumoPuntosRequest;
import com.coralclubes.facil.modules.clientes.dto.response.*;
import com.coralclubes.facil.modules.clientes.repository.PuntosRepository;
import com.coralclubes.facil.modules.cobranza.service.CobranzaGeneradorDocumentosService;
import com.coralclubes.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class PuntosService {
    private final PuntosRepository repo;
    private final SociosService sociosService;
    private final CobranzaGeneradorDocumentosService generadorDocumentosService;

    public PuntosMembresia obtenerPuntosMembresia(String membresia) {
        return repo.spSaldoPuntosDisponiblesMembresia(membresia);
    }

    public Integer consumirPuntos(ConsumoPuntosRequest request) {
        int pHospedaje = request.puntosHospedaje() != null ? request.puntosHospedaje() : 0;
        int pInstalaciones = request.puntosInstalaciones() != null ? request.puntosInstalaciones() : 0;
        int pGolf = request.puntosCampoGolf() != null ? request.puntosCampoGolf() : 0;

        if ((pHospedaje + pInstalaciones + pGolf) != request.totalPuntos()) {
            throw new IllegalArgumentException("La suma del desglose de puntos (" + (pHospedaje + pInstalaciones + pGolf) + ") no coincide con el total solicitado (" + request.totalPuntos() + ").");
        }

        // 2. Validar que el cliente tenga saldo
        PuntosMembresia saldoActual = obtenerPuntosMembresia(request.membresia());
        if (saldoActual == null || saldoActual.saldoPuntosNeto() < request.totalPuntos()) {
            throw new IllegalArgumentException("La membresía no cuenta con los puntos suficientes. Saldo actual: " + (saldoActual != null ? saldoActual.saldoPuntosNeto() : 0));
        }

        // 3. Ejecutar el Stored Procedure
        return repo.spCliConsumirPuntos(request);
    }

    public List<ConsumoPuntosDto> obtenerConsumoDePuntos(String membresia, LocalDateTime fechaCorte) {
        LocalDateTime fechaCorteFinal = fechaCorte != null ? fechaCorte : LocalDateTime.now();
        return repo.spClienteObtenerConsumoDePuntos(membresia, fechaCorteFinal);
    }

    public List<PuntosLiberadosDto> obtenerPuntosLiberados(String membresia, LocalDateTime fechaCorte) {
        LocalDateTime fechaCorteFinal = fechaCorte != null ? fechaCorte : LocalDateTime.now();
        return repo.spClienteObtenerPuntosLiberados(membresia, fechaCorteFinal);
    }

    public List<CuentaPuntosDto> obtenerCuentaDePuntos(String membresia, LocalDateTime fechaCorte) {
        LocalDateTime fechaCorteFinal = fechaCorte != null ? fechaCorte : LocalDateTime.now();
        return repo.spClienteObtenerCuentaDePuntos(membresia, fechaCorteFinal);
    }

    public List<DocumentoPdfDto> generarPdfsPuntos(String membresia, LocalDateTime fechaCorte) {
        LocalDateTime fechaCorteFinal = fechaCorte != null ? fechaCorte : LocalDateTime.now();

        // 1. Consultar información del socio
        ApiResponse<InformacionSocio> apiResponseSocio = sociosService.obtenerSocios(membresia);
        InformacionSocio socio = apiResponseSocio != null ? apiResponseSocio.data() : null;
        if (socio == null) {
            throw new IllegalArgumentException("No se encontró información para el socio con membresía: " + membresia);
        }

        // Formateadores
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaEmisionStr = LocalDate.now().format(dateFormatter);

        // Obtener listas
        List<ConsumoPuntosDto> consumosRaw = repo.spClienteObtenerConsumoDePuntos(membresia, fechaCorteFinal);
        List<PuntosLiberadosDto> liberadosRaw = repo.spClienteObtenerPuntosLiberados(membresia, fechaCorteFinal);
        List<CuentaPuntosDto> cuentaRaw = repo.spClienteObtenerCuentaDePuntos(membresia, fechaCorteFinal);

        // Periodo
        String periodoInicio = "Inicio";
        String periodoFin = fechaCorteFinal.format(dateFormatter);
        if (!cuentaRaw.isEmpty()) {
            LocalDateTime minDate = cuentaRaw.stream()
                    .map(CuentaPuntosDto::fechaInicio)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(fechaCorteFinal);
            periodoInicio = minDate.format(dateFormatter);
        }

        // ==================== 1. CONSUMO_PUNTOS ====================
        List<ConsumoPuntosPdfItemDto> consumosPdfItems = new ArrayList<>();
        for (var c : consumosRaw) {
            consumosPdfItems.add(ConsumoPuntosPdfItemDto.builder()
                    .fechaConsumo(c.fechaConsumo() != null ? c.fechaConsumo().format(dateTimeFormatter) : "")
                    .accesoClub(c.nombreBeneficiario() != null ? c.nombreBeneficiario() : "")
                    .desarrolloUso(c.desarrolloConsumo() != null ? c.desarrolloConsumo() : "")
                    .tipoCliente(c.tipoClienteAcceso() != null ? c.tipoClienteAcceso() : "")
                    .zonaAcceso(c.tipoAccesoDesarrollo() != null ? c.tipoAccesoDesarrollo() : "")
                    .periodoAcceso(c.periodoUsoDesarrollo() != null ? c.periodoUsoDesarrollo() : "")
                    .puntosHospedaje(c.puntosHospedaje() != null ? c.puntosHospedaje().toString() : "0")
                    .puntosInstalaciones(c.puntosInstalaciones() != null ? c.puntosInstalaciones().toString() : "0")
                    .puntosCampoGolf(c.puntosCampoGolf() != null ? c.puntosCampoGolf().toString() : "0")
                    .numeroAutorizacion(c.numeroAutorizacion() != null ? c.numeroAutorizacion() : "")
                    .descripcionReferencia(c.descripcionMovimiento() != null ? c.descripcionMovimiento() : "")
                    .build());
        }

        ConsumoPuntosPdfDto consumoPuntosPdfDto = ConsumoPuntosPdfDto.builder()
                .razonSocial("CORAL CLUBES")
                .slogan("CREANDO MOMENTOS INOLVIDABLES")
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .fechaEmision(fechaEmisionStr)
                .titular(socio.nombreCompleto() != null ? socio.nombreCompleto() : "")
                .membresia(socio.membresia() != null ? socio.membresia() : "")
                .tipoMembresia(socio.tipoMembresia() != null ? socio.tipoMembresia() : "")
                .desarrollo(socio.desarrollo() != null ? socio.desarrollo() : "")
                .clasificacionMembresia(socio.clasificacionMembresia() != null ? socio.clasificacionMembresia() : "")
                .domicilioSocio(socio.direccion() != null ? socio.direccion() : "")
                .consumosPuntos(consumosPdfItems)
                .build();

        byte[] pdfConsumo = generadorDocumentosService.generarPdfConsumoPuntos(consumoPuntosPdfDto);

        // ==================== 2. PUNTOS_LIBERADOS ====================
        List<PuntosLiberadosPdfItemDto> liberadosPdfItems = new ArrayList<>();
        int totalLiberadosPeriodo = 0;
        for (var l : liberadosRaw) {
            int pts = l.cantidadPuntos() != null ? l.cantidadPuntos() : 0;
            totalLiberadosPeriodo += pts;
            liberadosPdfItems.add(PuntosLiberadosPdfItemDto.builder()
                    .numeroPlan(l.numeroPlan() != null ? l.numeroPlan().toString() : "")
                    .fechaLiberacion(l.fechaLiberacion() != null ? l.fechaLiberacion().format(dateFormatter) : "")
                    .concepto(l.conceptoLiberacion() != null ? l.conceptoLiberacion() : "")
                    .puntos(String.valueOf(pts))
                    .recibo(l.folioRecibo() != null ? l.folioRecibo() : "")
                    .build());
        }

        PuntosLiberadosPdfResumenDto resumenPuntos = PuntosLiberadosPdfResumenDto.builder()
                .puntosLiberadosPeriodo(String.valueOf(totalLiberadosPeriodo))
                .puntosLiberadosPrevios("0")
                .totalPuntosLiberados(String.valueOf(totalLiberadosPeriodo))
                .build();

        PuntosLiberadosPdfDto puntosLiberadosPdfDto = PuntosLiberadosPdfDto.builder()
                .razonSocial("CORAL CLUBES")
                .slogan("CREANDO MOMENTOS INOLVIDABLES")
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .fechaEmision(fechaEmisionStr)
                .titular(socio.nombreCompleto() != null ? socio.nombreCompleto() : "")
                .membresia(socio.membresia() != null ? socio.membresia() : "")
                .tipoMembresia(socio.tipoMembresia() != null ? socio.tipoMembresia() : "")
                .desarrollo(socio.desarrollo() != null ? socio.desarrollo() : "")
                .clasificacionMembresia(socio.clasificacionMembresia() != null ? socio.clasificacionMembresia() : "")
                .domicilioSocio(socio.direccion() != null ? socio.direccion() : "")
                .puntosLiberados(liberadosPdfItems)
                .resumenPuntos(resumenPuntos)
                .build();

        byte[] pdfLiberados = generadorDocumentosService.generarPdfPuntosLiberados(puntosLiberadosPdfDto);

        // ==================== 3. ESTADO_CUENTA_PUNTOS ====================
        List<EstadoCuentaPuntosPdfItemDto> cuentaPdfItems = new ArrayList<>();
        int sumPuntosMembresia = 0;
        int sumPuntosEnganche = 0;
        int sumPuntosLiberados = 0;
        int sumPuntosConsumidos = 0;
        int sumPuntosHospedaje = 0;
        int sumPuntosInstalaciones = 0;
        int sumPuntosGolf = 0;
        int sumSaldoPuntosLibres = 0;

        for (var m : cuentaRaw) {
            int pMemb = m.puntosMembresia() != null ? m.puntosMembresia() : 0;
            int pEng = m.puntosEnganche() != null ? m.puntosEnganche() : 0;
            int pMens = m.puntosMensualidades() != null ? m.puntosMensualidades() : 0;
            int pLib = m.puntosLiberados() != null ? m.puntosLiberados() : 0;
            int pCons = m.puntosConsumidos() != null ? m.puntosConsumidos() : 0;
            int pHosp = m.puntosHospedaje() != null ? m.puntosHospedaje() : 0;
            int pInst = m.puntosInstalaciones() != null ? m.puntosInstalaciones() : 0;
            int pGolf = m.puntosCampoGolf() != null ? m.puntosCampoGolf() : 0;
            int sldLib = m.saldoPuntos() != null ? m.saldoPuntos() : 0;

            sumPuntosMembresia += pMemb;
            sumPuntosEnganche += pEng;
            sumPuntosLiberados += pLib;
            sumPuntosConsumidos += pCons;
            sumPuntosHospedaje += pHosp;
            sumPuntosInstalaciones += pInst;
            sumPuntosGolf += pGolf;
            sumSaldoPuntosLibres += sldLib;

            cuentaPdfItems.add(EstadoCuentaPuntosPdfItemDto.builder()
                    .planVenta(m.numeroPlan() != null ? m.numeroPlan().toString() : "")
                    .inicioVigencia(m.fechaInicio() != null ? m.fechaInicio().format(dateFormatter) : "")
                    .finVigencia(m.finalVigencia() != null ? m.finalVigencia().format(dateFormatter) : "")
                    .puntosMembresia(String.valueOf(pMemb))
                    .puntosEnganche(String.valueOf(pEng))
                    .puntosMensualidades(String.valueOf(pMens))
                    .descripcionMovimiento(m.descripcionMovimiento() != null ? m.descripcionMovimiento() : "")
                    .puntosLiberados(String.valueOf(pLib))
                    .puntosConsumidos(String.valueOf(pCons))
                    .puntosHospedaje(String.valueOf(pHosp))
                    .puntosInstalaciones(String.valueOf(pInst))
                    .puntosGolf(String.valueOf(pGolf))
                    .saldoPuntosLibres(String.valueOf(sldLib))
                    .estatusPuntos(m.estatusPuntos() != null ? m.estatusPuntos() : "")
                    .build());
        }

        EstadoCuentaPuntosPdfTotalesDto totalesGenerales = EstadoCuentaPuntosPdfTotalesDto.builder()
                .totalPuntosMembresia(String.valueOf(sumPuntosMembresia))
                .totalPuntosEnganche(String.valueOf(sumPuntosEnganche))
                .totalPuntosLiberados(String.valueOf(sumPuntosLiberados))
                .totalPuntosConsumidos(String.valueOf(sumPuntosConsumidos))
                .totalPuntosHospedaje(String.valueOf(sumPuntosHospedaje))
                .totalPuntosInstalaciones(String.valueOf(sumPuntosInstalaciones))
                .totalPuntosGolf(String.valueOf(sumPuntosGolf))
                .saldoPuntosLibres(String.valueOf(sumSaldoPuntosLibres))
                .build();

        EstadoCuentaPuntosPdfDto estadoCuentaPuntosPdfDto = EstadoCuentaPuntosPdfDto.builder()
                .razonSocial("CORAL CLUBES")
                .slogan("CREANDO MOMENTOS INOLVIDABLES")
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .fechaEmision(fechaEmisionStr)
                .titular(socio.nombreCompleto() != null ? socio.nombreCompleto() : "")
                .membresia(socio.membresia() != null ? socio.membresia() : "")
                .tipoMembresia(socio.tipoMembresia() != null ? socio.tipoMembresia() : "")
                .desarrollo(socio.desarrollo() != null ? socio.desarrollo() : "")
                .clasificacionMembresia(socio.clasificacionMembresia() != null ? socio.clasificacionMembresia() : "")
                .domicilioSocio(socio.direccion() != null ? socio.direccion() : "")
                .movimientosGeneralPuntos(cuentaPdfItems)
                .totalesGenerales(totalesGenerales)
                .build();

        byte[] pdfEstadoCuenta = generadorDocumentosService.generarPdfEstadoCuentaPuntos(estadoCuentaPuntosPdfDto);

        List<DocumentoPdfDto> result = new ArrayList<>();
        result.add(DocumentoPdfDto.builder()
                .nombre("consumo-puntos-" + membresia + ".pdf")
                .contenido(Base64.getEncoder().encodeToString(pdfConsumo))
                .build());
        result.add(DocumentoPdfDto.builder()
                .nombre("puntos-liberados-" + membresia + ".pdf")
                .contenido(Base64.getEncoder().encodeToString(pdfLiberados))
                .build());
        result.add(DocumentoPdfDto.builder()
                .nombre("estado-cuenta-puntos-" + membresia + ".pdf")
                .contenido(Base64.getEncoder().encodeToString(pdfEstadoCuenta))
                .build());

        return result;
    }
}
