package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.service.PuntosService;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosMembresia;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.HistoricoMovimientosRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.repository.MovimientosClienteRepository;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MovimientosClienteService {

    private final MovimientosClienteRepository repository;
    private final SociosService sociosService;
    private final CobranzaGeneradorDocumentosService generadorDocumentosService;
    private final PuntosService puntosService;

    public ApiResponse<List<EstadoCuentaAdeudoDto>> obtenerEstadoCuentaAdeudo(EstadoCuentaAdeudoRequest request, Integer desarrolloUsuario) {
        return ApiResponse.success(
                "Estado de cuenta por adeudo obtenido con éxito.",
                repository.spFacilObtenerEstadoCuentaAdeudo(request, desarrolloUsuario)
        );
    }

    public List<MovimientoHistoricoDto> obtenerHistoricoMovimientos(HistoricoMovimientosRequest request) {
        return repository.spCobranzaObtenerHistoricoMovimientos(request);
    }

    public List<MovimientoHistoricoPdfDto> obtenerHistoricoMovimientosPdf(String membresia, LocalDateTime fechaCorte) {
        return repository.spCobranzaObtenerHistoricoMovimientosPdf(membresia, fechaCorte);
    }

    public byte[] generarPdfEstadoCuentaSocio(String membresia) {
        // 1. Consultar información del socio
        ApiResponse<InformacionSocio> apiResponseSocio = sociosService.obtenerSocios(membresia);
        InformacionSocio socio = apiResponseSocio != null ? apiResponseSocio.data() : null;
        if (socio == null) {
            throw new IllegalArgumentException("No se encontró información para el socio con membresía: " + membresia);
        }

        // 2. Consultar movimientos por adeudo
        EstadoCuentaAdeudoRequest requestAdeudo = EstadoCuentaAdeudoRequest.builder()
                .membresia(membresia)
                .fechaCorte(null)
                .build();
        ApiResponse<List<EstadoCuentaAdeudoDto>> apiResponseAdeudo = this.obtenerEstadoCuentaAdeudo(requestAdeudo, 0);
        List<EstadoCuentaAdeudoDto> list = apiResponseAdeudo != null ? apiResponseAdeudo.data() : List.of();

        // 3. Filtrar movimientos con fecha de vencimiento a fin de mes
        LocalDate finMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime finMesTime = finMes.atTime(23, 59, 59);

        List<EstadoCuentaAdeudoDto> movimientosFiltrados = list.stream()
                .filter(mov -> mov.fechaVencimiento() != null && !mov.fechaVencimiento().isAfter(finMesTime))
                .toList();

        // 4. Fechas y Periodos
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate today = LocalDate.now();
        LocalDate inicioMes = today.with(TemporalAdjusters.firstDayOfMonth());

        String periodoInicio = inicioMes.format(dateFormatter);
        String periodoFin = finMes.format(dateFormatter);
        String fechaEmision = today.format(dateFormatter);

        // 5. Mapear movimientos y calcular totales
        DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
        BigDecimal sumCargos = BigDecimal.ZERO;
        BigDecimal sumInteres = BigDecimal.ZERO;
        BigDecimal sumNeto = BigDecimal.ZERO;

        List<MovimientoEstadoCuentaDto> movimientosPdf = new ArrayList<>();
        for (var mov : movimientosFiltrados) {
            sumCargos = sumCargos.add(mov.importeCargo());
            sumInteres = sumInteres.add(mov.interesMoratorio());
            sumNeto = sumNeto.add(mov.totalAPagar());

            String fechaStr = mov.fechaGeneracion() != null
                    ? mov.fechaGeneracion().toLocalDate().toString()
                    : "";
            String fechaVencStr = mov.fechaVencimiento() != null
                    ? mov.fechaVencimiento().toLocalDate().toString()
                    : "";

            movimientosPdf.add(MovimientoEstadoCuentaDto.builder()
                    .fecha(fechaStr)
                    .fechaVencimiento(fechaVencStr)
                    .concepto(mov.detalle())
                    .montoCargo(moneyFormat.format(mov.importeCargo()))
                    .montoInteres(moneyFormat.format(mov.interesMoratorio()))
                    .montoPendiente(moneyFormat.format(mov.totalAPagar()))
                    .build());
        }

        ResumenTotalesEstadoCuentaDto resumenTotales = ResumenTotalesEstadoCuentaDto.builder()
                .totalCargos(moneyFormat.format(sumCargos))
                .totalIntereses(moneyFormat.format(sumInteres))
                .totalNetoExigible(moneyFormat.format(sumNeto))
                .build();

        // 5.5 Obtener puntos de la membresía
        Integer puntosLiberados = null;
        Integer puntosConsumidos = null;
        Integer puntosDisponibles = null;
        try {
            PuntosMembresia puntos = puntosService.obtenerPuntosMembresia(membresia);
            if (puntos != null) {
                int liberados = puntos.totalPuntosLiberados() != null ? puntos.totalPuntosLiberados() : 0;
                int consumidos = puntos.puntosConsumidos() != null ? puntos.puntosConsumidos() : 0;
                int disponibles = puntos.saldoPuntosNeto() != null ? puntos.saldoPuntosNeto() : 0;

                if (liberados != 0 || consumidos != 0 || disponibles != 0) {
                    puntosLiberados = liberados;
                    puntosConsumidos = consumidos;
                    puntosDisponibles = disponibles;
                }
            }
        } catch (Exception e) {
            // Continuar con los campos de puntos en null si falla el servicio
        }

        // 6. Construir DTO principal para el generador
        DatosEstadoCuentaDto datos = DatosEstadoCuentaDto.builder()
                .razonSocial("CORAL CLUBES")
                .slogan("CREANDO MOMENTOS INOLVIDABLES")
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .fechaEmision(fechaEmision)
                .titular(socio.nombreCompleto())
                .membresia(socio.membresia())
                .tipoMembresia(socio.tipoMembresia())
                .telefonoContacto(socio.telefono())
                .correoContacto(socio.correo())
                .domicilioSocio(socio.direccion())
                .movimientos(movimientosPdf)
                .resumenTotales(resumenTotales)
                .puntosLiberados(puntosLiberados)
                .puntosConsumidos(puntosConsumidos)
                .puntosDisponibles(puntosDisponibles)
                .build();

        // 7. Generar y retornar el PDF
        return generadorDocumentosService.generarPdfEstadoCuenta(datos);
    }

    public byte[] generarPdfEstadoCuentaHistoricoSocio(String membresia, LocalDateTime fechaCorte) {
        // 1. Consultar información del socio
        ApiResponse<InformacionSocio> apiResponseSocio = sociosService.obtenerSocios(membresia);
        InformacionSocio socio = apiResponseSocio != null ? apiResponseSocio.data() : null;
        if (socio == null) {
            throw new IllegalArgumentException("No se encontró información para el socio con membresía: " + membresia);
        }

        // 2. Consultar movimientos históricos del socio
        LocalDateTime fechaCorteFinal = fechaCorte != null ? fechaCorte : LocalDateTime.now();
        List<MovimientoHistoricoPdfDto> rawList = repository.spCobranzaObtenerHistoricoMovimientosPdf(membresia, fechaCorteFinal);

        // 3. Fechas y Periodos
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate today = LocalDate.now();
        String fechaEmision = today.format(dateFormatter);

        String periodoInicio = "01/01/" + today.getYear();
        if (!rawList.isEmpty()) {
            LocalDateTime earliest = null;
            for (var m : rawList) {
                if (m.fechaGeneracion() != null) {
                    if (earliest == null || m.fechaGeneracion().isBefore(earliest)) {
                        earliest = m.fechaGeneracion();
                    }
                }
            }
            if (earliest != null) {
                periodoInicio = earliest.format(dateFormatter);
            }
        }
        String periodoFin = fechaCorteFinal.format(dateFormatter);

        // 4. Obtener puntos de la membresía
        Integer puntosLiberados = null;
        Integer puntosConsumidos = null;
        Integer puntosDisponibles = null;
        try {
            PuntosMembresia puntos = puntosService.obtenerPuntosMembresia(membresia);
            if (puntos != null) {
                int liberados = puntos.totalPuntosLiberados() != null ? puntos.totalPuntosLiberados() : 0;
                int consumidos = puntos.puntosConsumidos() != null ? puntos.puntosConsumidos() : 0;
                int disponibles = puntos.saldoPuntosNeto() != null ? puntos.saldoPuntosNeto() : 0;

                if (liberados != 0 || consumidos != 0 || disponibles != 0) {
                    puntosLiberados = liberados;
                    puntosConsumidos = consumidos;
                    puntosDisponibles = disponibles;
                }
            }
        } catch (Exception e) {
            // Continuar con los campos de puntos en null si falla el servicio
        }

        // 5. Construir jerarquía de movimientos
        List<MovimientoHistoricoTreeDto> padresTree = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#,##0.00");

        java.util.Map<Long, List<MovimientoHistoricoPdfDto>> childrenByParentId = new java.util.HashMap<>();
        for (var mov : rawList) {
            if (mov.padreId() != null && mov.padreId() != 0) {
                childrenByParentId.computeIfAbsent(Long.valueOf(mov.padreId()), k -> new ArrayList<>()).add(mov);
            }
        }

        for (var rawParent : rawList) {
            if (rawParent.padreId() == null || rawParent.padreId() == 0) {
                List<MovimientoHistoricoTreeDto> hijosTree = new ArrayList<>();
                List<MovimientoHistoricoPdfDto> rawHijos = childrenByParentId.getOrDefault(rawParent.id(), List.of());

                for (var rawHijo : rawHijos) {
                    List<MovimientoHistoricoTreeDto> nietosTree = new ArrayList<>();
                    List<MovimientoHistoricoPdfDto> rawNietos = childrenByParentId.getOrDefault(rawHijo.id(), List.of());

                    for (var rawNieto : rawNietos) {
                        nietosTree.add(MovimientoHistoricoTreeDto.builder()
                                .fechaVencimiento(rawNieto.fechaVencimiento() != null ? rawNieto.fechaVencimiento().toLocalDate().toString() : "")
                                .concepto(rawNieto.descripcionMovimiento() != null ? rawNieto.descripcionMovimiento() : "")
                                .montoCargo(df.format(rawNieto.importeCargo()))
                                .montoAbono(df.format(rawNieto.importeAbono()))
                                .montoInteres(df.format(rawNieto.interesMoratorio()))
                                .montoPendiente(df.format(rawNieto.importePendiente()))
                                .recibo(rawNieto.folioRecibo() != null ? rawNieto.folioRecibo() : "")
                                .fechaPago(rawNieto.fechaPagoRecibo() != null ? rawNieto.fechaPagoRecibo().toLocalDate().toString() : "")
                                .hijos(List.of())
                                .nietos(List.of())
                                .build());
                    }

                    hijosTree.add(MovimientoHistoricoTreeDto.builder()
                            .fechaVencimiento(rawHijo.fechaVencimiento() != null ? rawHijo.fechaVencimiento().toLocalDate().toString() : "")
                            .concepto(rawHijo.descripcionMovimiento() != null ? rawHijo.descripcionMovimiento() : "")
                            .montoCargo(df.format(rawHijo.importeCargo()))
                            .montoAbono(df.format(rawHijo.importeAbono()))
                            .montoInteres(df.format(rawHijo.interesMoratorio()))
                            .montoPendiente(df.format(rawHijo.importePendiente()))
                            .recibo(rawHijo.folioRecibo() != null ? rawHijo.folioRecibo() : "")
                            .fechaPago(rawHijo.fechaPagoRecibo() != null ? rawHijo.fechaPagoRecibo().toLocalDate().toString() : "")
                            .hijos(List.of())
                            .nietos(nietosTree)
                            .build());
                }

                padresTree.add(MovimientoHistoricoTreeDto.builder()
                        .fechaVencimiento(rawParent.fechaVencimiento() != null ? rawParent.fechaVencimiento().toLocalDate().toString() : "")
                        .concepto(rawParent.descripcionMovimiento() != null ? rawParent.descripcionMovimiento() : "")
                        .montoCargo(df.format(rawParent.importeCargo()))
                        .montoAbono(df.format(rawParent.importeAbono()))
                        .montoInteres(df.format(rawParent.interesMoratorio()))
                        .montoPendiente(df.format(rawParent.importePendiente()))
                        .recibo(rawParent.folioRecibo() != null ? rawParent.folioRecibo() : "")
                        .fechaPago(rawParent.fechaPagoRecibo() != null ? rawParent.fechaPagoRecibo().toLocalDate().toString() : "")
                        .hijos(hijosTree)
                        .nietos(List.of())
                        .build());
            }
        }

        // 6. Construir DTO principal para el generador
        DatosEstadoCuentaHistoricoDto datosHistoricos = DatosEstadoCuentaHistoricoDto.builder()
                .razonSocial("CORAL CLUBES")
                .slogan("CREANDO MOMENTOS INOLVIDABLES")
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .fechaEmision(fechaEmision)
                .titular(socio.nombreCompleto())
                .membresia(socio.membresia())
                .tipoMembresia(socio.tipoMembresia())
                .telefonoContacto(socio.telefono())
                .correoContacto(socio.correo())
                .domicilioSocio(socio.direccion())
                .puntosLiberados(puntosLiberados)
                .puntosConsumidos(puntosConsumidos)
                .puntosDisponibles(puntosDisponibles)
                .movimientosHistoricos(padresTree)
                .build();

        // 7. Generar y retornar el PDF
        return generadorDocumentosService.generarPdfEstadoCuentaHistorico(datosHistoricos);
    }
}
