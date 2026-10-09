package com.coralclubes.facil.shared.platform.templating.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.shared.platform.templating.dto.PlantillaCuerpoCorreo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlantillasCuerpoCorreoRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<PlantillaCuerpoCorreo> rowMapper = (rs, rowNum) -> PlantillaCuerpoCorreo.builder()
            .id(rs.getInt("id"))
            .codigo(rs.getString("codigo"))
            .nombre(rs.getString("nombre"))
            .descripcion(rs.getString("descripcion"))
            .asunto(rs.getString("asunto"))
            .cuerpo(rs.getString("cuerpo"))
            .activo(rs.getBoolean("activo"))
            .build();

    public Optional<PlantillaCuerpoCorreo> obtenerPorCodigo(String codigo) {
        return spExecutor.querySingle(
                "spFacilObtenerPlantillaCuerpoCorreoPorCodigo",
                Map.of("codigo", codigo),
                rowMapper
        );
    }
}
