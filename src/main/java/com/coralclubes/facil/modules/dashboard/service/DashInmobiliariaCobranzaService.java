package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashInmobiliariaCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosCobranzaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashInmobiliariaCobranzaService {

    private final DashInmobiliariaCobranzaRepository dashInmobiliariaCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashInmobiliariaTotales(String fechaInicio, String fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashInmobiliariaClasificacion(String fechaInicio, String fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaClasificacion(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosCobranzaResponse> spDashInmobiliariaCasas(String fechaInicio, String fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaCasas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosCobranzaResponse> spDashInmobiliariaTerrenos(String fechaInicio, String fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaTerrenos(fechaInicio, fechaFin);
    }
}
