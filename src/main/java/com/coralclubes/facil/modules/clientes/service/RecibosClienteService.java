package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.ReciboClienteDto;
import com.coralclubes.facil.modules.clientes.repository.RecibosClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecibosClienteService {

    private final RecibosClienteRepository repository;

    public List<ReciboClienteDto> obtenerRecibosMembresia(String membresia) {
        return repository.spCobranzaObtenerListadoRecibosMembresia(membresia);
    }
}
