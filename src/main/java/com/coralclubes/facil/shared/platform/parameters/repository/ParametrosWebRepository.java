package com.coralclubes.facil.shared.platform.parameters.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.shared.platform.parameters.dto.ParametrosWeb;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para la gestión de parámetros web del sistema.
 * Consume procedimientos almacenados en SQL Server para consultar configuraciones generales.
 */
@Repository
@RequiredArgsConstructor
public class ParametrosWebRepository {

    private final StoredProcedureExecutor spExecutor;

    /**
     * Mapeador de filas para transformar el resultado del SP en un objeto ParametrosWeb.
     */
    private final RowMapper<ParametrosWeb> parametroWebRowMapper = (rs, rowNum) -> new ParametrosWeb(
            rs.getString("clave"),
            rs.getString("valor")
    );

    /**
     * Consulta el valor de un parámetro web específico en la base de datos a través de su clave.
     *
     * @param clave Identificador único del parámetro web.
     * @return Optional con el valor del parámetro si existe.
     */
    public Optional<String> spFacilObtenerParametroWeb(String clave) {
        return spExecutor.querySingle(
                "spFacilObtenerParametroWeb",
                Map.of("clave", clave),
                (rs, rowNum) -> rs.getString("valor")
        );
    }

    /**
     * Consulta la lista completa de parámetros web configurados en el sistema.
     *
     * @return Lista de parámetros web encontrados.
     */
    public List<ParametrosWeb> spFacilObtenerParametrosWeb() {
        return spExecutor.queryList(
                "spFacilObtenerParametrosWeb",
                Map.of(),
                parametroWebRowMapper
        );
    }
}
