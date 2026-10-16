package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEventosCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.AdendasDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosOperadosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashEventosCobranzaService {

    private final DashEventosCobranzaRepository dashEventosCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerEventosTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEventosCobranzaRepository.spDashEventosTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerEventosDesarrollos(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEventosCobranzaRepository.spDashEventosDesarrollos(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> obtenerEventosDetalles(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosDetalles(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosOperadosResponse> obtenerEventosAmortizados(LocalDate fechaInicio, LocalDate fechaFin, String desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosAmortizados(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> obtenerEventosAnticipos(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosAnticipos(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerEventosAnticiposTotal(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosAnticiposTotal(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> obtenerEventosLiquidacion(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosLiquidacion(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerEventosLiquidacionTotal(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosLiquidacionTotal(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosOperadosResponse> obtenerEventosOperados(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosOperados(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerAdendasEventos(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEventosCobranzaRepository.spDashObtenerAdendasEventos(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<AdendasDetallesResponse> obtenerAdendasEventosDetalles(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashObtenerAdendasEventosDetalles(fechaInicio, fechaFin, desarrollo);
    }
}
