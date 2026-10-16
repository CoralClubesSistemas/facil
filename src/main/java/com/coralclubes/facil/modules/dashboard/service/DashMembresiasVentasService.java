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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashMembresiasVentasService {

    private final DashMembresiasVentasRepository dashMembresiasVentasRepository;

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerMembresiasDesarrollos(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashMembresiasVentasRepository.spDashMembresiasDesarrollos(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<MovimientoVentaXDesarrolloResponse> obtenerMovimientosVentasXDesarrollo(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        return dashMembresiasVentasRepository.spDashMovimientosVentasXDesarrollo(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerMembresiasProcesables(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashMembresiasVentasRepository.spDashMembresiasProcesables(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<MembresiasProcesablesDetallesResponse> obtenerMembresiasProcesablesDetalles(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        return dashMembresiasVentasRepository.spDashMembresiasProcesablesDetalles(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<MejoresClientesResponse> obtenerMejoresClientes(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashMembresiasVentasRepository.spDashMejoresClientes(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<UltimosClientesResponse> obtenerUltimosClientes(Integer desarrollo) {
        return dashMembresiasVentasRepository.spDashUltimosClientes(desarrollo);
    }

    @Transactional(readOnly = true)
    public List<UltimasMembresiasResponse> obtenerUltimasMembresias() {
        return dashMembresiasVentasRepository.spDashUltimasMembresias();
    }

    @Transactional(readOnly = true)
    public List<EstatusMembresiasResponse> obtenerEstatusMembresias(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashMembresiasVentasRepository.spDashEstatusMembresias(fechaInicio, fechaFin);
    }
}
