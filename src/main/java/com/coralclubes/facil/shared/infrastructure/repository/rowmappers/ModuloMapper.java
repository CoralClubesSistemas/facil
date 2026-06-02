package com.coralclubes.facil.shared.infrastructure.repository.rowmappers;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ModuloMapper implements RowMapper<ModuloDtoResult> {
    @Override
    public ModuloDtoResult mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ModuloDtoResult.builder()
                .id((long) rs.getInt("ID"))
                .idPadre(rs.getObject("PADRE_ID") != null ? (long) rs.getInt("PADRE_ID") : null)
                .clave(rs.getString("CLAVE"))
                .nombre(rs.getString("NOMBRE"))
                .ruta(rs.getString("RUTA"))
                .icono(rs.getString("ICONO"))
                .menuFacil(rs.getInt("MENU_FACIL"))
                .orden((long) rs.getInt("ORDEN"))
                .build();
    }
}
