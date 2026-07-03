package com.coralclubes.facil.modules.sistema.repository;

import com.coralclubes.facil.modules.sistema.dto.response.PlantillaCuerpoCorreo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlantillasCuerpoCorreoRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PlantillaCuerpoCorreo> rowMapper = (rs, rowNum) -> PlantillaCuerpoCorreo.builder()
            .codigo(rs.getString("codigo"))
            .descripcion(rs.getString("descripcion"))
            .asunto(rs.getString("asunto"))
            .cuerpo(rs.getString("cuerpo"))
            .activo(rs.getBoolean("activo"))
            .build();

    public Optional<PlantillaCuerpoCorreo> obtenerPorCodigo(String codigo) {
        String sql = "SELECT codigo, descripcion, asunto, cuerpo, activo FROM plantillas_cuerpo_correo WHERE codigo = ? AND activo = 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, codigo));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
