package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEventosVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosOperadosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashEventosVentasService {

    private final DashEventosVentasRepository dashEventosVentasRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashEventosTotalesVentas(String fechaInicio, String fechaFin) {
        return dashEventosVentasRepository.spDashEventosTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashEventosDesarrollosVentas(String fechaInicio, String fechaFin) {
        return dashEventosVentasRepository.spDashEventosDesarrollosVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> spDashEventosDetallesVentas(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosVentasRepository.spDashEventosDetallesVentas(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosOperadosResponse> spDashEventosVendidos(String fechaInicio, String fechaFin, String desarrollo) {
        return dashEventosVentasRepository.spDashEventosVendidos(fechaInicio, fechaFin, desarrollo);
    }
}
