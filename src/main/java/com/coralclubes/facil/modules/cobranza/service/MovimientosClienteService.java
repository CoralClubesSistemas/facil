package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
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

    public ApiResponse<List<EstadoCuentaAdeudoDto>> obtenerEstadoCuentaAdeudo(EstadoCuentaAdeudoRequest request, Integer desarrolloUsuario) {
        return ApiResponse.success(
                "Estado de cuenta por adeudo obtenido con éxito.",
                repository.spFacilObtenerEstadoCuentaAdeudo(request, desarrolloUsuario)
        );
    }

    public List<MovimientoHistoricoDto> obtenerHistoricoMovimientos(HistoricoMovimientosRequest request) {
        return repository.spCobranzaObtenerHistoricoMovimientos(request);
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
                .build();

        // 7. Generar y retornar el PDF
        return generadorDocumentosService.generarPdfEstadoCuenta(datos);
    }
}
