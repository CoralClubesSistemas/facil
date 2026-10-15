package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashCarteraCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InventarioMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashCarteraCobranzaService {

    private final DashCarteraCobranzaRepository dashCarteraCobranzaRepository;

    @Transactional(readOnly = true)
    public List<InventarioMembresiasResponse> spObtieneInventarioMembresiasTotales(String fechaInicio, String fechaFin) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InventarioMembresiasResponse> spObtieneInventarioMembresiasDesarrollo(String fechaInicio, String fechaFin, String desarrollo) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasDesarrollo(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spInventarioMembresiasTotalesGlobal(String fechaInicio, String fechaFin) {
        return dashCarteraCobranzaRepository.spInventarioMembresiasTotalesGlobal(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spObtieneInventarioMembresiasTotalesXCartera(String fechaInicio, String fechaFin, String desarrollo) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasTotalesXCartera(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spObtieneInventarioMembresiasTotalesXTipo(String fechaInicio, String fechaFin, String desarrollo) {
        return dashCarteraCobranzaRepository.spObtieneInventarioMembresiasTotalesXTipo(fechaInicio, fechaFin, desarrollo);
    }
}
