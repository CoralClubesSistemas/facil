package com.coralclubes.facil.modules.usuarios.service;

import com.coralclubes.facil.modules.usuarios.repository.UsuariosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuariosRepository repo;

    public Optional<String> obtenerCorreoUsuario(String usuario) {
        return repo.spFacilObtenerCorreoUsuario(usuario);
    }
}
