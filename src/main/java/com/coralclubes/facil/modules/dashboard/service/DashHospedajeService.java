package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.repository.DashHospedajeRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.HospedajeDetalleMovimientoResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.HospedajeLibreResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.HospedajesDisponiblesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.ReservasCanceladasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.SinReservaDetalleMovimientoResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashHospedajeService {

    private final DashHospedajeRepository dashHospedajeRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> spDashHospedajeTotales(String fechaInicio, String fechaFin) {
        return dashHospedajeRepository.spDashHospedajeTotales(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashHospedajeTotalesEstatus(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashHospedajeRepository.spDashHospedajeTotalesEstatus(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<TotalCategoriaResponse<Long>> spDashHospedajesDesarrollos(String fechaInicio, String fechaFin) {
        return dashHospedajeRepository.spDashHospedajesDesarrollos(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<HospedajeLibreResponse> spDashHospedajesLibres(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashHospedajeRepository.spDashHospedajesLibres(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<HospedajesDisponiblesResponse> spDashDisponibilidadxMes(String mes, String anio, String desarrollo) {
        return dashHospedajeRepository.spDashDisponibilidadxMes(mes, anio, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<HospedajeDetalleMovimientoResponse> spDashHospedajesDetallesMovimientos(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashHospedajeRepository.spDashHospedajesDetallesMovimientos(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<ReservasCanceladasResponse> spDashReservasCanceladas(String idDesarrollo, String fechaEntrada, String fechaSalida) {
        return dashHospedajeRepository.spDashReservasCanceladas(idDesarrollo, fechaEntrada, fechaSalida);
    }

    @Transactional(readOnly = true)
    public List<SinReservaDetalleMovimientoResponse> spDashSinReservaDetallesMovimientos(String fechaInicio, String fechaFin, Integer desarrollo) {
        return dashHospedajeRepository.spDashSinReservaDetallesMovimientos(fechaInicio, fechaFin, desarrollo);
    }
}
