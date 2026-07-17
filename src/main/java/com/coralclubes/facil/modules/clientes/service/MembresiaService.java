package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.*;
import com.coralclubes.facil.modules.clientes.repository.MembresiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembresiaService {

    private final MembresiaRepository repository;

    public Optional<MembresiaCancelacionDto> obtenerDatosCancelacion(String membresia) {
        return repository.spMembresiaObtenerDatosCancelacion(membresia);
    }

    public Optional<MembresiaAfiliacionDto> obtenerAfiliacionCargoAutomatico(String membresia) {
        return repository.spMembresiaAfiliacionCargoAutomatico(membresia);
    }

    public Optional<MembresiaVigenciaDto> obtenerVigencia(String membresia) {
        return repository.spMembresiaObtenerVigencia(membresia);
    }

    public Optional<MembresiaAccesosFinSemanaDto> obtenerAccesosFinDeSemana(String membresia) {
        return repository.spMembresiaObtenerAccesosFinDeSemana(membresia);
    }

    public Optional<MembresiaDetallesPlanVentaDto> obtenerDetallesPlanVenta(String membresia, Integer plan) {
        return repository.spMembresiaObtenerDetallesPlanVenta(membresia, plan);
    }

    public Optional<MembresiaDetalleProcesableDto> obtenerDetalleProcesable(String membresia) {
        return repository.spMembresiaObtenerDetalleProcesable(membresia);
    }

    public List<MembresiaTemporalDto> obtenerMembresiasTemporales(String membresia) {
        return repository.spMembresiaObtenerMembresiasTemporales(membresia);
    }
}
