package com.coralclubes.facil.modules.manuales.repository;

import com.coralclubes.facil.modules.manuales.dto.request.ManualRequest;
import com.coralclubes.facil.modules.manuales.dto.request.VersionRequest;
import com.coralclubes.facil.modules.manuales.dto.response.ManualResponse;
import com.coralclubes.facil.modules.manuales.dto.response.VersionResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ManualesRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<ManualResponse> manualMapper = (rs, rowNum) -> ManualResponse.builder()
            .id(rs.getInt("Id"))
            .nombre(rs.getString("Nombre"))
            .descripcion(rs.getString("Descripcion"))
            .moduloId(rs.getInt("ModuloId"))
            .moduloNombre(rs.getString("ModuloNombre"))
            .versionId(rs.getObject("VersionId") != null ? rs.getInt("VersionId") : null)
            .version(rs.getObject("Version") != null ? rs.getInt("Version") : null)
            .archivoUuid(rs.getString("ArchivoUuid") != null ? UUID.fromString(rs.getString("ArchivoUuid")) : null)
            .nombreArchivo(rs.getString("NombreArchivo"))
            .tipo(rs.getString("Tipo"))
            .build();

    private final RowMapper<VersionResponse> versionMapper = (rs, rowNum) -> VersionResponse.builder()
            .id(rs.getInt("Id"))
            .version(rs.getInt("Version"))
            .cambios(rs.getString("Cambios"))
            .archivoUuid(rs.getString("ArchivoUuid") != null ? UUID.fromString(rs.getString("ArchivoUuid")) : null)
            .nombreArchivo(rs.getString("NombreArchivo"))
            .tipo(rs.getString("Tipo"))
            .esActual(rs.getBoolean("EsActual"))
            .fecha(rs.getTimestamp("Fecha").toLocalDateTime())
            .build();

    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);
    private final RowMapper<UUID> uuidMapper = (rs, rowNum) -> UUID.fromString(rs.getString(1));

    public List<ManualResponse> obtenerManuales(Integer moduloId) {
        return spExecutor.queryList("spMnlObtenerManuales", Map.of("MdlId", moduloId != null ? moduloId : 0), manualMapper);
    }

    public Optional<Integer> guardarManual(ManualRequest request, String usuario) {
        Map<String, Object> params = Map.of(
                "Id", request.id() != null ? request.id() : 0,
                "Nombre", request.nombre(),
                "Descripcion", request.descripcion() != null ? request.descripcion() : "",
                "ModuloId", request.moduloId(),
                "Usuario", usuario
        );
        return spExecutor.querySingle("spMnlGuardarManual", params, scalarIntMapper);
    }

    public List<UUID> eliminarManual(Integer id) {
        return spExecutor.queryList("spMnlEliminarManual", Map.of("Id", id), uuidMapper);
    }

    public Optional<Integer> publicarVersion(VersionRequest request) {
        Map<String, Object> params = Map.of(
                "ManualId", request.manualId(),
                "Version", request.version(),
                "Cambios", request.cambios() != null ? request.cambios() : "",
                "Uuid", request.uuid(),
                "NombreArchivo", request.nombreArchivo(),
                "Tipo", request.tipo()
        );
        return spExecutor.querySingle("spMnlPublicarVersion", params, scalarIntMapper);
    }

    public List<VersionResponse> obtenerVersiones(Integer manualId) {
        return spExecutor.queryList("spMnlObtenerVersiones", Map.of("ManualId", manualId), versionMapper);
    }
}
