package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.request.AsignarCuponesMembresiaRequest;
import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleAsignacionResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaDetalleResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaResumenResponse;
import com.coralclubes.facil.modules.clientes.repository.CuponesMembresiasRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuponesMembresiasService {

    private final CuponesMembresiasRepository repository;
    private final BusinessLogger logger;

    public List<CuponMembresiaResumenResponse> obtenerCuponesMembresia(String membresia, Integer year) {
        return repository.spMembresiaObtenerCupones(membresia, year);
    }

    public List<CuponMembresiaDetalleResponse> obtenerDetalleCuponMembresia(Integer cuponId, String membresia) {
        return repository.spMembresiaObtenerDetalleCupon(cuponId, membresia);
    }

    public List<CuponDisponibleAsignacionResponse> obtenerDisponiblesParaAsignacion(
            String membresia,
            Integer desarrollo,
            Integer tipoMembresiaId,
            Integer anio,
            LocalDateTime fechaAsignacion
    ) {
        return repository.spCuponesObtenerDisponiblesParaAsignacion(membresia, desarrollo, tipoMembresiaId, anio, fechaAsignacion);
    }

    public void asignarCuponesAMembresia(AsignarCuponesMembresiaRequest request, String usuario) {
        logger.info(usuario, "Asignando cupones a membresia: {} en desarrollo: {}", request.membresia(), request.desarrollo());
        repository.spCuponesAsignarAMembresia(request, usuario);
    }
}
