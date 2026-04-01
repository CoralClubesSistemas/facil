package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.shared.infrastructure.security.repository.SeguridadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeguridadService {

    private final SeguridadRepository repository;

    public List<String> obtenerUsernamesPorPermisoYDesarrollo(String claveModulo, Integer idDesarrollo) {
        return repository.obtenerUsernamesPorPermisoYDesarrollo(claveModulo, idDesarrollo);
    }
}