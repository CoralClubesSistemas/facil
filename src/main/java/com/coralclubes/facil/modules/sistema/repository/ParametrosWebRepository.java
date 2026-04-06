package com.coralclubes.facil.modules.sistema.repository;

import com.coralclubes.facil.modules.sistema.dto.response.ParametrosWeb;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para la gestión de parámetros web del sistema.
 * Accede a datos generales de configuración del aplicativo.
 */
@Repository
@RequiredArgsConstructor
public class ParametrosWebRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<String> parametroWebMapper = (rs, rowNum) -> rs.getString("Valor");

    private final RowMapper<ParametrosWeb> parametroWebRowMapper = (rs, rowNum) -> new ParametrosWeb(
            rs.getString("Clave"),
            rs.getString("Valor")
    );

    /**
     * Obtiene el valor de un parámetro web por su clave.
     * Consulta la tabla PARAMETROS_WEB filtrando por la clave especificada.
     *
     * @param clave Identificador único del parámetro a consultar (ej: 'VERSION', 'TIMEOUT', etc.)
     * @return Optional con el ParametroWebResponse si existe, vacío en caso contrario
     */
    public Optional<String> spFacilObtenerParametroWeb(String clave) {
        return spExecutor.querySingle(
                "spFacilObtenerParametroWeb",
                Map.of("Clave", clave),
                parametroWebMapper
        );
    }

    public List<ParametrosWeb> spFacilObtenerParametrosWeb() {
        return spExecutor.queryList(
                "spFacilObtenerParametrosWeb",
                Map.of(),
                parametroWebRowMapper
        );
    }
}

