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

    public List<CuponMembresiaDetalleResponse> obtenerDetalleCuponMembresia(Integer cuponId) {
        return repository.spMembresiaObtenerDetalleCupon(cuponId);
    }

    public List<CuponDisponibleAsignacionResponse> obtenerDisponiblesParaAsignacion(
            String membresia,
            String origen,
            Integer anio
    ) {
        return repository.spCuponesObtenerDisponiblesParaAsignacion(membresia, origen, anio);
    }

    public void asignarCuponesAMembresia(AsignarCuponesMembresiaRequest request, String usuario) {
        logger.info(usuario, "Asignando cupones a membresía: {} con origen: {}", request.membresia(), request.origen());
        repository.spCuponesAsignarAMembresia(request, usuario);
    }

    public void bloquearCuponMembresia(Integer cuponId, Integer folio, String usuario) {
        logger.info(usuario, "Bloqueando folio: {} del cupón ID: {}", folio, cuponId);
        repository.spMembresiaBloquearCupon(cuponId, folio, usuario);
    }
}
