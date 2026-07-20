package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.*;
import com.coralclubes.facil.modules.clientes.repository.MembresiaRepository;
import com.coralclubes.facil.modules.reportes.service.ReportesByKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembresiaService {

    private final MembresiaRepository repository;
    private final ReportesByKeyService reportesByKeyService;

    public Optional<MembresiaCancelacionDto> obtenerDatosCancelacion(String membresia) {
        return repository.spMembresiaObtenerDatosCancelacion(membresia);
    }

    public Optional<MembresiaAfiliacionDto> obtenerAfiliacionCargoAutomatico(String membresia) {
        return repository.spMembresiaAfiliacionCargoAutomatico(membresia);
    }

    public Optional<MembresiaVigenciaDto> obtenerVigencia(String membresia) {
        return repository.spMembresiaObtenerVigencia(membresia);
    }

    public Optional<MembresiaAccesosFinSemanaDto> obtenerAccesosFinDeSemana(String membresia) {
        return repository.spMembresiaObtenerAccesosFinDeSemana(membresia);
    }

    public Optional<MembresiaDetallesPlanVentaDto> obtenerDetallesPlanVenta(String membresia, Integer plan) {
        return repository.spMembresiaObtenerDetallesPlanVenta(membresia, plan);
    }

    public Optional<MembresiaDetalleProcesableDto> obtenerDetalleProcesable(String membresia) {
        return repository.spMembresiaObtenerDetalleProcesable(membresia);
    }

    public List<MembresiaTemporalDto> obtenerMembresiasTemporales(String membresia) {
        return repository.spMembresiaObtenerMembresiasTemporales(membresia);
    }

    public List<MembresiaAccesoDto> obtenerAccesos(
            String membresia,
            String desarrollo,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Boolean soloFS,
            Integer numeroPagina,
            Integer registrosPorPagina
    ) {
        return repository.spMembresiaObtenerAccesos(membresia, desarrollo, fechaDesde, fechaHasta, soloFS, numeroPagina, registrosPorPagina);
    }

    public List<MembresiaAccesoEntradaSalidaDto> obtenerAccesosEntradasSalidas(
            String membresia,
            Integer desarrollo,
            LocalDate fechaAccesoDesde,
            Integer beneficiario
    ) {
        return repository.spMembresiaObtenerAccesosEntradasSalidas(membresia, desarrollo, fechaAccesoDesde, beneficiario);
    }

    public List<MembresiaReferidoDto> obtenerReferidos(String membresia) {
        return repository.spMembresiaObtenerReferidos(membresia);
    }

    public byte[] generarReporteAccesosExcel(Map<String, Object> variables) {
        return reportesByKeyService.generarReporteByKey(
                com.coralclubes.facil.modules.reportes.enums.keysReportes.ACCESOS_MEMBRESIA.getClave(),
                variables
        );
    }
}
