package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.repository.GestionOrdenCobranzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GestionOrdenCobranzaService {
    private final GestionOrdenCobranzaRepository repo;


}
