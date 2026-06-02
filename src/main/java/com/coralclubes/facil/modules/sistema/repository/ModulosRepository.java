package com.coralclubes.facil.modules.sistema.repository;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.shared.infrastructure.repository.rowmappers.ModuloMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repositorio para la gestión de módulos en el sistema.
 */
@Repository
@RequiredArgsConstructor
public class ModulosRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<ModuloDtoResult> moduloMapper = new ModuloMapper();

    /**
     * Mapper para obtener un solo entero (ID generado o status).
     */
    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    // =========================================================================
    // MÉTODOS
    // =========================================================================

    /**
     * Obtiene todos los módulos del sistema.
     *
     * @return Lista de ModuloDto con los módulos del sistema.
     */
    public List<ModuloDtoResult> spFacilObtenerModulosSistema() {
        return spExecutor.queryList(
                "spFacilObtenerModulosSistema",
                null,
                moduloMapper
        );
    }

    /**
     * Guarda un nuevo módulo en el sistema.
     *
     * @param modulo Datos del módulo a guardar.
     * @return ID del módulo guardado.
     */
    public Integer spFacilGuardarModulo(ModuloDtoResult modulo) {
        // Usamos Map.of para una construcción limpia de parámetros (Java 9+)
        Map<String, Object> params = Map.of(
                "ID", modulo.id() != null ? modulo.id() : 0, // Manejo seguro de ID nulo para nuevos registros
                "ID_PADRE", modulo.idPadre() != null ? modulo.idPadre() : 0,
                "CLAVE", modulo.clave(),
                "NOMBRE", modulo.nombre(),
                "RUTA", modulo.ruta(),
                "ICONO", modulo.icono(),
                "MENU_FACIL", modulo.menuFacil()
        );

        // Usamos querySingle porque esperamos que el SP retorne el ID generado en un ResultSet de 1x1
        return spExecutor.querySingle(
                "spFacilGuardarModulo",
                params,
                scalarIntMapper
        ).orElse(null); // Retorna null si falla o no devuelve nada
    }

    /**
     * Eliminación de modulo por ID.
     *
     * @param idModulo ID del modulo a eliminar
     * @return Entero indicando el resultado (1 éxito, o el ID eliminado).
     */
    public Integer spFacilEliminarModulo(Integer idModulo) {
        return spExecutor.querySingle(
                "spFacilEliminarModulo",
                Map.of("MODULO_ID", idModulo),
                scalarIntMapper
        ).orElse(null);
    }
}