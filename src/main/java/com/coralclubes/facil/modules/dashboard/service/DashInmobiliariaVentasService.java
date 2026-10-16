package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashInmobiliariaVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashInmobiliariaVentasService {

    private final DashInmobiliariaVentasRepository dashInmobiliariaVentasRepository;

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> obtenerInmobiliariaClasificacionVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaVentasRepository.spDashInmobiliariaClasificacionVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosResponse> obtenerInmobiliariaCasasVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaVentasRepository.spDashInmobiliariaCasasVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaMovimientosResponse> obtenerInmobiliariaTerrenosVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaVentasRepository.spDashInmobiliariaTerrenosVentas(fechaInicio, fechaFin);
    }
}
