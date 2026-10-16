package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashCarteraCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InventarioMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashCarteraCobranzaService {

    private final DashCarteraCobranzaRepository dashCarteraCobranzaRepository;

    @Transactional(readOnly = true)
    public List<InventarioMembresiasResponse> obtenerInventarioMembresiasTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InventarioMembresiasResponse> obtenerInventarioMembresiasDesarrollo(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasDesarrollo(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerInventarioMembresiasTotalesGlobal(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashCarteraCobranzaRepository.spInventarioMembresiasTotalesGlobal(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerInventarioMembresiasTotalesXCartera(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasTotalesXCartera(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerInventarioMembresiasTotalesXTipo(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasTotalesXTipo(fechaInicio, fechaFin, desarrollo);
    }
}
