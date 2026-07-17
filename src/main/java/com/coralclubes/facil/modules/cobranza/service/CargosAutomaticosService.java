package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.response.RechazoCAResponse;
import com.coralclubes.facil.modules.cobranza.repository.CargosAutomaticosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CargosAutomaticosService {

    private final CargosAutomaticosRepository repository;

    public List<RechazoCAResponse> obtenerRechazosCA(String membresia) {
        return repository.spCobranzaObtenerRechazosCA(membresia);
    }
}
