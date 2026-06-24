package com.coralclubes.facil.modules.usuarios.service;

import com.coralclubes.facil.modules.usuarios.repository.SeguridadRepository;
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