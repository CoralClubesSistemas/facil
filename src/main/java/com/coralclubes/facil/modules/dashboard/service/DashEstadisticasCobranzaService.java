package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEstadisticasCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashEstadisticasCobranzaService {

    private final DashEstadisticasCobranzaRepository dashEstadisticasCobranzaRepository;

    @Transactional(readOnly = true)
    public Optional<IngresosTotalesResponse> spDashBoardIngresosTotales(String fechaInicio, String fechaFin) {
        return dashEstadisticasCobranzaRepository.spDashBoardIngresosTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesResponse> spDashEstadisticasTotales(Integer anio) {
        return dashEstadisticasCobranzaRepository.spDashEstadisticasTotales(anio);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesResponse> spDashEstadisticasTotalesXDesarrollo(Integer anio, String desarrollo) {
        return dashEstadisticasCobranzaRepository.spDashEstadisticasTotalesXDesarrollo(anio, desarrollo);
    }
}
