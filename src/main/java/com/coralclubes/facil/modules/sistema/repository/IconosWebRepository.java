package com.coralclubes.facil.modules.sistema.repository;

import com.coralclubes.facil.shared.domain.dto.IconoWeb;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class IconosWebRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<IconoWeb> iconoWebMapper = (rs, rowNum) -> new IconoWeb(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("icono")
    );

    public List<IconoWeb> spFacilCatalogoIconosWeb () {
        return executor.queryList("spFacilCatalogoIconosWeb", Map.of(), iconoWebMapper);
    }
}
