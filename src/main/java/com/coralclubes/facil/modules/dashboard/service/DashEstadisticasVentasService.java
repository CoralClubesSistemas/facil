package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashEstadisticasVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
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

    @Transactional(readOnly = true)
    public List<DetallesMovimientosResponse> spDashDetallesMovimientos(String fechaInicio, String fechaFin, String desarrollo, String movimientos) {
        return dashEstadisticasVentasRepository.spDashDetallesMovimientos(fechaInicio, fechaFin, desarrollo, movimientos);
    }
}
