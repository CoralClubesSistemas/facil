package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashGolfVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesPromocionesXFechaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesXDiaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashGolfVentasService {

    private final DashGolfVentasRepository dashGolfVentasRepository;

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerGreenFeesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<GreenFeesXDiaResponse> obtenerGreenFeesXDiaVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesXDiaVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<GreenFeesXDiaResponse> obtenerGreenFeesXPteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesXPteVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerGreenFeesPromociones(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesPromociones(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<DetallesMovimientosResponse> obtenerGreenFeesPromocionesDetalles(LocalDate fechaInicio, LocalDate fechaFin, String promocion) {
        return dashGolfVentasRepository.spDashGreenFeesPromocionesDetalles(fechaInicio, fechaFin, promocion);
    }

    @Transactional(readOnly = true)
    public List<GreenFeesPromocionesXFechaResponse> obtenerGreenFeesPromocionesXFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesPromocionesXFecha(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerTorneosPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfVentasRepository.spDashTorneosPeriodo(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerTorneosTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashGolfVentasRepository.spDashTorneosTotales(fechaInicio, fechaFin);
    }
}
