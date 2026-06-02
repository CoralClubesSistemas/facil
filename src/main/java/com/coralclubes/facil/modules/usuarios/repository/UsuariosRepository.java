package com.coralclubes.facil.modules.usuarios.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.shared.infrastructure.repository.rowmappers.ModuloMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuariosRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<String> campoString = (rs, rowNum) -> rs.getString(1);

    private final RowMapper<ModuloDtoResult> moduloMapper = new ModuloMapper();

    public List<ModuloDtoResult> spLoginModulosUsuarios(String usuario) {
        return executor.queryList("spLoginModulosUsuarios", Map.of("USUARIO", usuario), moduloMapper);
    }

    public Optional<String> spFacilObtenerCorreoUsuario(String usuario) {
        return executor.querySingle("spFacilObtenerCorreoUsuario",
                Map.of("Usuario", usuario),
                campoString);
    }

    @org.springframework.cache.annotation.Cacheable(value = "preferencias_usuario", key = "#usuario", unless = "#result == null")
    public Optional<String> spUserObtenerPreferencias(String usuario) {
        return executor.querySingle("spUserObtenerPreferencias",
                Map.of("Usuario", usuario),
                campoString);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "preferencias_usuario", key = "#usuario")
    public void spUserActualizarPreferencias(String usuario, String preferenciasJson) {
        executor.execute("spUserActualizarPreferencias",
                Map.of(
                        "Usuario", usuario,
                        "preferencias_json", preferenciasJson
                ));
    }
}
