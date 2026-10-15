package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashInmobiliariaVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashInmobiliariaVentasService {

    private final DashInmobiliariaVentasRepository dashInmobiliariaVentasRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashInmobiliariaTotalesVentas(String fechaInicio, String fechaFin) {
        return dashInmobiliariaVentasRepository.spDashInmobiliariaTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashInmobiliariaClasificacionVentas(String fechaInicio, String fechaFin) {
        return dashInmobiliariaVentasRepository.spDashInmobiliariaClasificacionVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosResponse> spDashInmobiliariaCasasVentas(String fechaInicio, String fechaFin) {
        return dashInmobiliariaVentasRepository.spDashInmobiliariaCasasVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosResponse> spDashInmobiliariaTerrenosVentas(String fechaInicio, String fechaFin) {
        return dashInmobiliariaVentasRepository.spDashInmobiliariaTerrenosVentas(fechaInicio, fechaFin);
    }
}
