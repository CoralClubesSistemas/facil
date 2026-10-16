package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEstadisticasVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashEstadisticasVentasService {

    private final DashEstadisticasVentasRepository dashEstadisticasVentasRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashMembresiasTotalesVentas(String fechaInicio, String fechaFin) {
        return dashEstadisticasVentasRepository.spDashMembresiasTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashGolfTotalesVentas(String fechaInicio, String fechaFin) {
        return dashEstadisticasVentasRepository.spDashGolfTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashEventosTotalesVentas(String fechaInicio, String fechaFin) {
        return dashEstadisticasVentasRepository.spDashEventosTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashInmobiliariaTotalesVentas(String fechaInicio, String fechaFin) {
        return dashEstadisticasVentasRepository.spDashInmobiliariaTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<IngresosTotalesResponse> spDashBoardIngresosTotalesVentas(String fechaInicio, String fechaFin) {
        return dashEstadisticasVentasRepository.spDashBoardIngresosTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesResponse> spDashEstadisticasTotalesVentas(Integer anio) {
        return dashEstadisticasVentasRepository.spDashEstadisticasTotalesVentas(anio);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesResponse> spDashEstadisticasTotalesXDesarrolloVentas(Integer anio, String desarrollo) {
        return dashEstadisticasVentasRepository.spDashEstadisticasTotalesXDesarrolloVentas(anio, desarrollo);
    }
}
