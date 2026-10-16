package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashClasificacionesCobranzaRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashClasificacionesCobranzaService {

    private final DashClasificacionesCobranzaRepository dashClasificacionesCobranzaRepository;

    @Transactional(readOnly = true)
    public List<ClasificacionesResponse> obtenerTotalesClasificaciones(LocalDate fechaInicio, LocalDate fechaFin, String desarrollos, String clasificaciones) {
        return dashClasificacionesCobranzaRepository.spDashTotalesClasificaciones(fechaInicio, fechaFin, desarrollos, clasificaciones);
    }

    @Transactional(readOnly = true)
    public List<ClasificacionesDetallesResponse> obtenerClasificacionesDetalles(LocalDate fechaInicio, LocalDate fechaFin, String desarrollos, String clasificaciones) {
        return dashClasificacionesCobranzaRepository.spDashClasificacionesDetalles(fechaInicio, fechaFin, desarrollos, clasificaciones);
    }
}
