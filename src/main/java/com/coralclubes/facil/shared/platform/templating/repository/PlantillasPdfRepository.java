package com.coralclubes.facil.shared.platform.templating.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.shared.platform.templating.dto.PlantillaPdfProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PlantillasPdfRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<PlantillaPdfProjection> rowMapper = (rs, rowNum) -> new PlantillaPdfProjection(
            rs.getInt("id"),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getString("contenido"),
            rs.getBoolean("activo")
    );

    public PlantillaPdfProjection obtenerPorCodigo(String codigo) {
        return spExecutor.querySingle(
                "spFacilObtenerPlantillaPdfPorCodigo",
                Map.of("codigo", codigo),
                rowMapper
        ).orElseThrow(() -> new RuntimeException("Plantilla no encontrada: " + codigo));
    }
}
