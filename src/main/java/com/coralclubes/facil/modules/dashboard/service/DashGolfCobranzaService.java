package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashGolfCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashGolfCobranzaService {

    private final DashGolfCobranzaRepository dashGolfCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerGolfTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfCobranzaRepository.spDashGolfTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerGreenFeesTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfCobranzaRepository.spDashGreenFeesTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerGreenFeesClasificacion(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfCobranzaRepository.spDashGreenFeesClasificacion(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerOtrosIngresosGolf(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfCobranzaRepository.spDashOtrosIngresosGolf(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerOtrosIngresosGolfDetalles(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfCobranzaRepository.spDashOtrosIngresosGolfDetalles(fechaInicio, fechaFin);
    }
}
