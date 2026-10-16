package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEstadisticasVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashEstadisticasVentasService {

    private final DashEstadisticasVentasRepository dashEstadisticasVentasRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerMembresiasTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashMembresiasTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerGolfTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashGolfTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerEventosTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashEventosTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerInmobiliariaTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashInmobiliariaTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<IngresosTotalesResponse> obtenerIngresosTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEstadisticasVentasRepository.spDashBoardIngresosTotalesVentas(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesResponse> obtenerEstadisticasTotalesVentas(Integer anio) {
        return dashEstadisticasVentasRepository.spDashEstadisticasTotalesVentas(anio);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesResponse> obtenerEstadisticasTotalesXDesarrolloVentas(Integer anio, String desarrollo) {
        return dashEstadisticasVentasRepository.spDashEstadisticasTotalesXDesarrolloVentas(anio, desarrollo);
    }
}
