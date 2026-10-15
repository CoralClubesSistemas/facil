package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEventosCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.AdendasDetallesResponse;
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
public class DashEventosCobranzaService {

    private final DashEventosCobranzaRepository dashEventosCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashEventosTotales(String fechaInicio, String fechaFin) {
        return dashEventosCobranzaRepository.spDashEventosTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashEventosDesarrollos(String fechaInicio, String fechaFin) {
        return dashEventosCobranzaRepository.spDashEventosDesarrollos(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> spDashEventosDetalles(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosDetalles(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosOperadosResponse> spDashEventosAmortizados(String fechaInicio, String fechaFin, String desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosAmortizados(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> spDashEventosAnticipos(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosAnticipos(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashEventosAnticiposTotal(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosAnticiposTotal(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosDetallesResponse> spDashEventosLiquidacion(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosLiquidacion(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashEventosLiquidacionTotal(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosLiquidacionTotal(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EventosOperadosResponse> spDashEventosOperados(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashEventosOperados(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashObtenerAdendasEventos(String fechaInicio, String fechaFin) {
        return dashEventosCobranzaRepository.spDashObtenerAdendasEventos(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<AdendasDetallesResponse> spDashObtenerAdendasEventosDetalles(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashEventosCobranzaRepository.spDashObtenerAdendasEventosDetalles(fechaInicio, fechaFin, desarrollo);
    }
}
