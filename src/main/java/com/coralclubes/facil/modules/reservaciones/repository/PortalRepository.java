package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.projection.ExperienciaPortalProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarExperienciaRequest;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PortalRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    private final RowMapper<ExperienciaPortalProjection> experienciaMapper = (rs, rowNum) ->
            ExperienciaPortalProjection.builder()
                    .id(rs.getInt("id"))
                    .tag(rs.getString("tag"))
                    .titulo(rs.getString("titulo"))
                    .descripcion(rs.getString("descripcion"))
                    .link(rs.getString("link"))
                    .img(rs.getString("img"))
                    .build();

    public List<ExperienciaPortalProjection> spResvObtenerExperienciasPortal() {
        return spExecutor.queryList("spResvObtenerExperienciasPortal", Map.of(), experienciaMapper);
    }

    public Optional<Integer> spResvGuardarExperienciasPortal(GuardarExperienciaRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", request.id());
        params.put("tag", request.tag());
        params.put("titulo", request.titulo());
        params.put("descripcion", request.descripcion());
        params.put("link", request.link());
        params.put("img", request.img());

        return spExecutor.querySingleLog("spResvGuardarExperienciasPortal", params, scalarIntMapper, usuario, false, true);
    }

    public void spResvEliminarExperienciasPortal(Integer id, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        spExecutor.executeLog("spResvEliminarExperienciasPortal", params, usuario, false, true);
    }
}
