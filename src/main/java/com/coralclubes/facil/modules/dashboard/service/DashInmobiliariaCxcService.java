package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashInmobiliariaCxcRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetalleClienteResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarTotalesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashInmobiliariaCxcService {

    private final DashInmobiliariaCxcRepository dashInmobiliariaCxcRepository;

    @Transactional(readOnly = true)
    public List<InmobiliariaXCobrarTotalesResponse> spDashInmobiliariaXCobrarTotales(String fechaInicio, String fechaFin) {
        return dashInmobiliariaCxcRepository.spDashInmobiliariaXCobrarTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaXCobrarDetallesResponse> spDashInmobiliariaXCobrarDetalles(String fechaInicio, String fechaFin, String clasificacion) {
        return dashInmobiliariaCxcRepository.spDashInmobiliariaXCobrarDetalles(fechaInicio, fechaFin, clasificacion);
    }

    @Transactional(readOnly = true)
    public List<InmobiliariaXCobrarDetalleClienteResponse> spDashInmobiliariaXCobrarDetallesXCliente(String fechaInicio, String fechaFin, String membresia) {
        return dashInmobiliariaCxcRepository.spDashInmobiliariaXCobrarDetallesXCliente(fechaInicio, fechaFin, membresia);
    }
}
