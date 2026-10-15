package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashMembresiasVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.EstatusMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.MejoresClientesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.MembresiasProcesablesDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.MovimientoVentaXDesarrolloResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.UltimasMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.UltimosClientesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashMembresiasVentasService {

    private final DashMembresiasVentasRepository dashMembresiasVentasRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashMembresiasTotalesVentas(String fechaInicio, String fechaFin) {
        return dashMembresiasVentasRepository.spDashMembresiasTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashMembresiasDesarrollos(String fechaInicio, String fechaFin) {
        return dashMembresiasVentasRepository.spDashMembresiasDesarrollos(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<MovimientoVentaXDesarrolloResponse> spDashMovimientosVentasXDesarrollo(String fechaInicio, String fechaFin, String desarrollo) {
        return dashMembresiasVentasRepository.spDashMovimientosVentasXDesarrollo(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashMembresiasProcesables(String fechaInicio, String fechaFin) {
        return dashMembresiasVentasRepository.spDashMembresiasProcesables(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<MembresiasProcesablesDetallesResponse> spDashMembresiasProcesablesDetalles(String fechaInicio, String fechaFin, String desarrollo) {
        return dashMembresiasVentasRepository.spDashMembresiasProcesablesDetalles(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<MejoresClientesResponse> spDashMejoresClientes(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashMembresiasVentasRepository.spDashMejoresClientes(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<UltimosClientesResponse> spDashUltimosClientes(Integer desarrollo) {
        return dashMembresiasVentasRepository.spDashUltimosClientes(desarrollo);
    }

    @Transactional(readOnly = true)
    public List<UltimasMembresiasResponse> spDashUltimasMembresias() {
        return dashMembresiasVentasRepository.spDashUltimasMembresias();
    }

    @Transactional(readOnly = true)
    public List<EstatusMembresiasResponse> spDashEstatusMembresias(String fechaInicio, String fechaFin) {
        return dashMembresiasVentasRepository.spDashEstatusMembresias(fechaInicio, fechaFin);
    }
}
