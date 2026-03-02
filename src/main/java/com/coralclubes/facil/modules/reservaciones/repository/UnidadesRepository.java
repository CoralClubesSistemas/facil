package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.dto.projection.ImagenDto;
import com.coralclubes.facil.modules.reservaciones.dto.projection.TipoUnidadCardDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TipoUnidadDetalleDto;
import com.coralclubes.facil.modules.reservaciones.dto.request.EliminarImagenRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.ImagenRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.RelacionCaracteristicaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.TipoUnidadRequest;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.coralclubes.facil.modules.reservaciones.dto.response.UnidadFisicaDto;
import com.coralclubes.facil.modules.reservaciones.dto.request.UnidadFisicaRequest;
import com.coralclubes.dto.SelectGenerico;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UnidadesRepository {
    private final StoredProcedureExecutor spExecutor;

    // =========================================================================
    // MAPPERS
    // =========================================================================

    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    private final RowMapper<TipoUnidadCardDto> tipoUnidadCardMapper = (rs, rowNum) -> TipoUnidadCardDto.builder()
            .idTipoUnidad(rs.getInt("ID_TIPO_UNIDAD"))
            .idLsvTipoUnidad(rs.getInt("ID_LSV_TIPO_UNIDAD"))
            .nombreTipoUnidad(rs.getString("NOMBRE_TIPO_UNIDAD"))
            .capacidad(rs.getInt("CAPACIDAD"))
            .descripcionCorta(rs.getString("DESCRIPCION_CORTA"))
            .uuidPortada(rs.getString("UUID_PORTADA") != null ? UUID.fromString(rs.getString("UUID_PORTADA")) : null)
            .calificacion(rs.getBigDecimal("CALIFICACION"))
            .idDesarrollo(rs.getInt("ID_DESARROLLO"))
            .nombreHotel(rs.getString("NOMBRE_DESARROLLO"))
            .build();

    private final RowMapper<TipoUnidadDetalleDto> tipoUnidadDetalleMapper = (rs, rowNum) -> TipoUnidadDetalleDto.builder()
            .idTipoUnidad(rs.getInt("ID_TIPO_UNIDAD"))
            .idDesarrollo(rs.getInt("ID_DESARROLLO"))
            .nombreDesarrollo(rs.getString("NOMBRE_DESARROLLO"))
            .idLsvTipoUnidad(rs.getInt("ID_LSV_TIPO_UNIDAD"))
            .nombreTipoUnidad(rs.getString("NOMBRE_TIPO_UNIDAD"))
            .capacidad(rs.getInt("CAPACIDAD"))
            .descripcionCorta(rs.getString("DESCRIPCION_CORTA"))
            .descripcionLarga(rs.getString("DESCRIPCION_LARGA"))
            .calificacion(rs.getBigDecimal("CALIFICACION"))
            .build();

    private final RowMapper<ImagenDto> imagenUnidadMapper = (rs, rowNum) -> ImagenDto.builder()
            .idImagen(rs.getInt("ID_IMAGEN"))
            .uuid(UUID.fromString(rs.getString("UUID")))
            .esPortada(rs.getBoolean("ES_PORTADA"))
            .orden(rs.getInt("ORDEN"))
            .build();

    private final RowMapper<CaracteristicaDto> caracteristicaMapper = (rs, rowNum) -> CaracteristicaDto.builder()
            .idCaracteristica(rs.getInt("ID_CARACTERISTICA"))
            .nombre(rs.getString("NOMBRE"))
            .descripcion(rs.getString("DESCRIPCION"))
            .icono(rs.getString("ICONO"))
            .idTipo(rs.getObject("ID_TIPO") != null ? rs.getInt("ID_TIPO") : null)
            .cantidad(rs.getInt("CANTIDAD"))
            .build();

    private final RowMapper<UnidadFisicaDto> unidadFisicaMapper = (rs, rowNum) -> UnidadFisicaDto.builder()
            .idUnidadFisica(rs.getInt("ID_UNIDAD_FISICA"))
            .numeroUnidad(rs.getString("NUMERO_UNIDAD"))
            .piso(rs.getObject("PISO") != null ? rs.getInt("PISO") : null)
            .idPadre(rs.getObject("ID_PADRE") != null ? rs.getInt("ID_PADRE") : null)
            .numeroPadre(rs.getString("NUMERO_PADRE"))
            .idTipoUnidad(rs.getInt("ID_TIPO_UNIDAD"))
            .nombreTipoUnidad(rs.getString("NOMBRE_TIPO_UNIDAD"))
            .idDesarrollo(rs.getInt("ID_DESARROLLO"))
            .nombreDesarrollo(rs.getString("NOMBRE_DESARROLLO"))
            .build();

    private final RowMapper<SelectGenerico<Integer>> posiblesPadresMapper = (rs, rowNum) -> new SelectGenerico<>(
            rs.getInt("value"),
            rs.getString("label")
    );

    // =========================================================================
    // MÉTODOS DE ESCRITURA (Write)
    // =========================================================================

    public Optional<Integer> spResvGuardarTipoUnidad(TipoUnidadRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdTipoUnidad", request.idTipoUnidad());
        params.put("IdLsvTipoUnidad", request.idLsvTipoUnidad());
        params.put("NombreTipoUnidad", request.nombreTipoUnidad());
        params.put("IdDesarrollo", request.idDesarrollo());
        params.put("Capacidad", request.capacidad());
        params.put("DescripcionLarga", request.descripcionLarga());
        params.put("DescripcionCorta", request.descripcionCorta());
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvGuardarTipoUnidad", params, scalarIntMapper, usuario, true, true);
    }

    public Optional<Integer> spResvGuardarCaracteristicasTipoUnidad(Integer tipoUnidadId, List<RelacionCaracteristicaRequest> caracteristicas, String usuario) {
        return spExecutor.querySingleLog("spResvGuardarCaracteristicasTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "Caracteristicas", JsonUtils.toJson(caracteristicas), "Usuario", usuario),
                scalarIntMapper, usuario, false, true);
    }

    public Optional<Integer> spResvGuardarImagenesTipoUnidad(Integer tipoUnidadId, List<ImagenRequest> imagenes, String usuario) {
        return spExecutor.querySingleLog("spResvGuardarImagenesTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "ImagenesJson", JsonUtils.toJson(imagenes), "Usuario", usuario),
                scalarIntMapper, usuario, false, true);
    }

    public boolean spResvEliminarImagenesTipoUnidad(Integer tipoUnidadId, List<EliminarImagenRequest> imagenesAEliminar, String usuario) {
        return spExecutor.executeLog("spResvEliminarImagenesTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "ImagenesJson", JsonUtils.toJson(imagenesAEliminar)),
                usuario, false, true);
    }

    public boolean spResvCambiarImagenPortadaTipoUnidad(Integer tipoUnidadId, UUID nuevaPortadaUuid, String usuario) {
        return spExecutor.executeLog("spResvCambiarImagenPortadaTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId,
                        "NuevaPortadaUuid", nuevaPortadaUuid.toString()
                ),
                usuario,
                false,
                false);
    }

    public boolean spResvDesactivarTipoUnidad(Integer tipoUnidadId, String usuario) {
        return spExecutor.executeLog("spResvDesactivarTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "Usuario", usuario),
                usuario, true, true);
    }


    // =========================================================================
    // MÉTODOS DE LECTURA (Read)
    // =========================================================================

    public List<TipoUnidadCardDto> spResvObtenerTiposUnidadCard(Integer idDesarrollo) {
        return spExecutor.queryList("spResvObtenerTiposUnidadCard", Map.of("IdDesarrollo", idDesarrollo), tipoUnidadCardMapper);
    }

    public Optional<TipoUnidadDetalleDto> spResvObtenerTipoUnidadDetalles(Integer idTipoUnidad) {
        return spExecutor.querySingle("spResvObtenerTipoUnidadDetalles", Map.of("IdTipoUnidad", idTipoUnidad), tipoUnidadDetalleMapper);
    }

    public List<ImagenDto> spResvObtenerTipoUnidadImagenes(Integer idTipoUnidad) {
        return spExecutor.queryList("spResvObtenerTipoUnidadImagenes", Map.of("IdTipoUnidad", idTipoUnidad), imagenUnidadMapper);
    }

    public List<CaracteristicaDto> spResvObtenerCaracteristicasXTipoUnidad(Integer idTipoUnidad) {
        return spExecutor.queryList("spResvObtenerCaracteristicasXTipoUnidad", Map.of("IdTipoUnidad", idTipoUnidad), caracteristicaMapper);
    }

    public Optional<Integer> spResvGuardarUnidadFisica(UnidadFisicaRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdUnidadFisica", request.idUnidadFisica());
        params.put("IdDesarrollo", request.idDesarrollo());
        params.put("IdTipoUnidad", request.idTipoUnidad());
        params.put("NumeroUnidad", request.numeroUnidad());
        params.put("Piso", request.piso());
        params.put("IdPadre", request.idPadre());
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvGuardarUnidadFisica", params, scalarIntMapper, usuario, true, true);
    }

    public Optional<Integer> spResvAsignarUnidadesFisicasATipo(Integer idTipoUnidad, List<Integer> idsUnidades, String usuario) {
        return spExecutor.querySingleLog("spResvAsignarUnidadesFisicasATipo",
                Map.of("IdTipoUnidad", idTipoUnidad, "IdsUnidadesJson", JsonUtils.toJson(idsUnidades), "Usuario", usuario),
                scalarIntMapper, usuario, false, true);
    }

    public Optional<Integer> spResvDesasignarUnidadesFisicas(List<Integer> idsUnidades, String usuario) {
        return spExecutor.querySingleLog("spResvDesasignarUnidadesFisicas",
                Map.of("IdsUnidadesJson", JsonUtils.toJson(idsUnidades), "Usuario", usuario),
                scalarIntMapper, usuario, false, true);
    }

    public boolean spResvDesactivarUnidadFisica(Integer idUnidadFisica, String usuario) {
        return spExecutor.executeLog("spResvDesactivarUnidadFisica",
                Map.of("IdUnidadFisica", idUnidadFisica, "Usuario", usuario),
                usuario, true, true);
    }

    public List<UnidadFisicaDto> spResvObtenerUnidadesFisicasXTipo(Integer idTipoUnidad) {
        return spExecutor.queryList("spResvObtenerUnidadesFisicasXTipo", Map.of("IdTipoUnidad", idTipoUnidad), unidadFisicaMapper);
    }

    public List<UnidadFisicaDto> spResvObtenerUnidadesFisicasDisponiblesXDesarrollo(Integer idDesarrollo) {
        return spExecutor.queryList("spResvObtenerUnidadesFisicasDisponiblesXDesarrollo", Map.of("IdDesarrollo", idDesarrollo), unidadFisicaMapper);
    }

    public List<SelectGenerico<Integer>> spResvObtenerCatalogoPosiblesPadres(Integer idDesarrollo, Integer idUnidadFisicaExcluida) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdDesarrollo", idDesarrollo);
        params.put("IdUnidadFisicaExcluida", idUnidadFisicaExcluida);
        return spExecutor.queryList("spResvObtenerCatalogoPosiblesPadres", params, posiblesPadresMapper);
    }
}
