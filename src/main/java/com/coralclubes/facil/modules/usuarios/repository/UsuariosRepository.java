package com.coralclubes.facil.modules.usuarios.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuariosRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<String> campoString = (rs, rowNum) -> rs.getString(1);

    public Optional<String> spFacilObtenerCorreoUsuario(String usuario) {
        return executor.querySingle("spFacilObtenerCorreoUsuario",
                Map.of("Usuario", usuario),
                campoString);
    }
}
