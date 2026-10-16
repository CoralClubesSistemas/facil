package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashInmobiliariaCxcRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetalleClienteResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarTotalesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashInmobiliariaCxcService {

    private final DashInmobiliariaCxcRepository dashInmobiliariaCxcRepository;

    @Transactional(readOnly = true)
    public List<InmobiliariaXCobrarTotalesResponse> obtenerInmobiliariaXCobrarTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashInmobiliariaCxcRepository.spDashInmobiliariaXCobrarTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaXCobrarDetallesResponse> obtenerInmobiliariaXCobrarDetalles(LocalDate fechaInicio, LocalDate fechaFin, String clasificacion) {
        return dashInmobiliariaCxcRepository.spDashInmobiliariaXCobrarDetalles(fechaInicio, fechaFin, clasificacion);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaXCobrarDetalleClienteResponse> obtenerInmobiliariaXCobrarDetallesXCliente(LocalDate fechaInicio, LocalDate fechaFin, String membresia) {
        return dashInmobiliariaCxcRepository.spDashInmobiliariaXCobrarDetallesXCliente(fechaInicio, fechaFin, membresia);
    }
}
