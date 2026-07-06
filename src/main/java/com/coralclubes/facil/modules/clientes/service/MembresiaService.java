package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.MembresiaDatosDto;
import com.coralclubes.facil.modules.clientes.repository.MembresiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembresiaService {

    private final MembresiaRepository repository;

    public Optional<MembresiaDatosDto> obtenerDatosMembresia(String membresia, Integer plan) {
        return repository.spCobranzaOntenerDatosMembresia(membresia, plan);
    }
}
