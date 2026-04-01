package com.coralclubes.facil.shared.infrastructure.security.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SeguridadRepository {
    private final StoredProcedureExecutor spExecutor;

    /**
     * Llama a un SP para obtener los usernames de los empleados activos
     * que tienen cierto permiso en un desarrollo específico.
     */
    public List<String> obtenerUsernamesPorPermisoYDesarrollo(String claveModulo, Integer idDesarrollo) {
        // Mapeamos directamente a String porque el SP solo devolverá una columna con los usernames
        return spExecutor.queryList(
                "spSegObtenerUsuariosPorPermiso",
                Map.of(
                        "ClaveModulo", claveModulo,
                        "IdDesarrollo", idDesarrollo
                ),
                (rs, rowNum) -> rs.getString("username")
        );
    }
}
