package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashGolfVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesPromocionesXFechaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesXDiaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashGolfVentasService {

    private final DashGolfVentasRepository dashGolfVentasRepository;


    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashGreenFeesVentas(String fechaInicio, String fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<GreenFeesXDiaResponse> spDashGreenFeesXDiaVentas(String fechaInicio, String fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesXDiaVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<GreenFeesXDiaResponse> spDashGreenFeesXPteVentas(String fechaInicio, String fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesXPteVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashGreenFeesPromociones(String fechaInicio, String fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesPromociones(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<DetallesMovimientosResponse> spDashGreenFeesPromocionesDetalles(String fechaInicio, String fechaFin, String promocion) {
        return dashGolfVentasRepository.spDashGreenFeesPromocionesDetalles(fechaInicio, fechaFin, promocion);
    }

    @Transactional(readOnly = true)
    public List<GreenFeesPromocionesXFechaResponse> spDashGreenFeesPromocionesXFecha(String fechaInicio, String fechaFin) {
        return dashGolfVentasRepository.spDashGreenFeesPromocionesXFecha(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashTorneosPeriodo(String fechaInicio, String fechaFin) {
        return dashGolfVentasRepository.spDashTorneosPeriodo(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashTorneosTotales(String fechaInicio, String fechaFin) {
        return dashGolfVentasRepository.spDashTorneosTotales(fechaInicio, fechaFin);
    }
}
