package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiosReferidosResponse;
import com.coralclubes.facil.modules.clientes.dto.response.DetalleConsumoReferidoResponse;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaReferidoDto;
import com.coralclubes.facil.modules.clientes.dto.response.ResumenReferidosResponse;
import com.coralclubes.facil.modules.clientes.repository.ReferidosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReferidosService {

    private final ReferidosRepository repository;

    public List<BeneficiosReferidosResponse> obtenerBeneficiosReferidos(String membresia) {
        return repository.spMembresiaObtenerBeneficiosReferidos(membresia);
    }

    public List<DetalleConsumoReferidoResponse> obtenerDetalleConsumoReferido(String membresiaReferidor, Integer consecutivoReferido) {
        return repository.spMembresiaObtenerDetalleConsumoReferido(membresiaReferidor, consecutivoReferido);
    }

    public Optional<ResumenReferidosResponse> obtenerResumenReferidos(String membresia) {
        return repository.spMembresiaObtenerResumenReferidos(membresia);
    }

    public List<MembresiaReferidoDto> obtenerReferidos(String membresia) {
        return repository.spMembresiaObtenerReferidos(membresia);
    }
}
