package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashMembresiasCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashMembresiasCobranzaService {

    private final DashMembresiasCobranzaRepository dashMembresiasCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerMembresiasTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashMembresiasCobranzaRepository.spDashMembresiasTotales(fechaInicio, fechaFin);
    }
}
