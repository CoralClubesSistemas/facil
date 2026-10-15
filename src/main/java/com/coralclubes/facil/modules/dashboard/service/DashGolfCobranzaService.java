package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashGolfCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashGolfCobranzaService {

    private final DashGolfCobranzaRepository dashGolfCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashGolfTotales(String fechaInicio, String fechaFin) {
        return dashGolfCobranzaRepository.spDashGolfTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashGreenFeesTotales(String fechaInicio, String fechaFin) {
        return dashGolfCobranzaRepository.spDashGreenFeesTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashGreenFeesClasificacion(String fechaInicio, String fechaFin) {
        return dashGolfCobranzaRepository.spDashGreenFeesClasificacion(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashOtrosIngresosGolf(String fechaInicio, String fechaFin) {
        return dashGolfCobranzaRepository.spDashOtrosIngresosGolf(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashOtrosIngresosGolfDetalles(String fechaInicio, String fechaFin) {
        return dashGolfCobranzaRepository.spDashOtrosIngresosGolfDetalles(fechaInicio, fechaFin);
    }
}
