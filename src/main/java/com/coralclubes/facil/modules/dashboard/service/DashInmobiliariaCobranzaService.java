package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashInmobiliariaCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosCobranzaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashInmobiliariaCobranzaService {

    private final DashInmobiliariaCobranzaRepository dashInmobiliariaCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerInmobiliariaTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerInmobiliariaClasificacion(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaClasificacion(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosCobranzaResponse> obtenerInmobiliariaCasas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaCasas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosCobranzaResponse> obtenerInmobiliariaTerrenos(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaCobranzaRepository.spDashInmobiliariaTerrenos(fechaInicio, fechaFin);
    }
}
