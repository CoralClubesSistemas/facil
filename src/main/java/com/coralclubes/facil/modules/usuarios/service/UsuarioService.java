package com.coralclubes.facil.modules.usuarios.service;

import com.coralclubes.facil.modules.usuarios.repository.UsuariosRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final UsuariosRepository repo;
    private final ObjectMapper objectMapper;

    public Optional<String> obtenerCorreoUsuario(String usuario) {
        return repo.spFacilObtenerCorreoUsuario(usuario);
    }

    public Map<String, Object> obtenerPreferenciasMap(String usuario) {
        Optional<String> jsonOpt = repo.spUserObtenerPreferencias(usuario);
        if (jsonOpt.isEmpty() || jsonOpt.get().isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(jsonOpt.get(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Error al deserializar las preferencias del usuario {}: {}", usuario, e.getMessage());
            return Collections.emptyMap();
        }
    }

    public void actualizarPreferenciasMap(String usuario, Map<String, Object> preferencias) {
        try {
            String json = objectMapper.writeValueAsString(preferencias);
            repo.spUserActualizarPreferencias(usuario, json);
        } catch (Exception e) {
            log.error("Error al serializar las preferencias del usuario {}: {}", usuario, e.getMessage());
            throw new RuntimeException("Error al guardar las preferencias de usuario", e);
        }
    }
}
