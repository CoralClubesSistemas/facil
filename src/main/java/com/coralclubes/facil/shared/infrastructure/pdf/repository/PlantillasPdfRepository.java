package com.coralclubes.facil.shared.infrastructure.pdf.repository;

import com.coralclubes.facil.shared.infrastructure.pdf.dto.PlantillaPdfProjection;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import org.springframework.cache.annotation.Cacheable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlantillasPdfRepository {
    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<PlantillaPdfProjection> mapper = (rs, rowNum) -> new PlantillaPdfProjection(
            rs.getInt("id"),
            rs.getString("codigo"),
            rs.getString("contenido")
    );

    public PlantillaPdfProjection obtenerPorCodigo(String codigo) {
        Map<String, Object> params = new HashMap<>();
        params.put("codigo", codigo);
        return spExecutor.querySingle("sp_obtener_plantilla_por_codigo", params, mapper)
                .orElseThrow(() -> new RuntimeException("Plantilla PDF no encontrada para el código: " + codigo));
    }
}
