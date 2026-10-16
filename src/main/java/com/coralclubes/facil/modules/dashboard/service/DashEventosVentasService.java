package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEventosVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosOperadosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashEventosVentasService {

    private final DashEventosVentasRepository dashEventosVentasRepository;

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerEventosDesarrollosVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEventosVentasRepository.spDashEventosDesarrollosVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> obtenerEventosDetallesVentas(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosVentasRepository.spDashEventosDetallesVentas(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosOperadosResponse> obtenerEventosVendidos(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        return dashEventosVentasRepository.spDashEventosVendidos(fechaInicio, fechaFin, desarrollo);
    }
}
