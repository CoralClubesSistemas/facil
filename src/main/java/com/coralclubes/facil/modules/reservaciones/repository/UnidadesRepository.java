package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.projection.TipoUnidadDetalles;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.shared.domain.dto.ImagenDto;
import com.coralclubes.facil.modules.reservaciones.dto.projection.TipoUnidadCardDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
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

    private final RowMapper<TipoUnidadDetalles> detallesUIRowMapper = ((rs, rowNum) -> TipoUnidadDetalles.builder()
            .rhdtId(rs.getInt("RhdtId"))
            .nombreTipoUnidad(rs.getString("NombreTipoUnidad"))
            .capacidad(rs.getInt("Capacidad"))
            .descripcionCorta(rs.getString("DescripcionCorta"))
            .descripcionLarga(rs.getString("DescripcionLarga"))
            .nombreDesarrollo(rs.getString("NombreDesarrollo"))
            .calificacion(rs.getBigDecimal("Calificacion"))
            .imagenesUUID(rs.getString("ImagenesUUID") != null ? JsonUtils.fromJson(rs.getString("ImagenesUUID"), new TypeReference<List<ImagenDto>>() {
            }) : List.of())
            .caracteristicas(rs.getString("Caracteristicas") != null ? JsonUtils.fromJson(rs.getString("Caracteristicas"), new TypeReference<List<CaracteristicaDto>>() {
            }) : List.of())

            .build());

    private final RowMapper<UnidadBloqueadaDto> unidadBloqueadaMapper = (rs, rowNum) -> new UnidadBloqueadaDto(
            rs.getInt("IdUnidadFisica"),
            rs.getString("NumeroUnidad"),
            rs.getString("Desarrollo"),
            rs.getString("TipoUnidad"),
            rs.getDate("FechaInicio") != null ? rs.getDate("FechaInicio").toLocalDate() : null,
            rs.getDate("FechaFin") != null ? rs.getDate("FechaFin").toLocalDate() : null,
            rs.getString("RazonBloqueo"),
            rs.getString("UsuarioBloqueo"),
            rs.getTimestamp("FechaRegistro") != null ? rs.getTimestamp("FechaRegistro").toLocalDateTime() : null,
            rs.getString("ComentarioLargo")
    );

    private final RowMapper<ArticuloAmenidadDto> articuloAmenidadMapper = (rs, rowNum) -> new ArticuloAmenidadDto(
            rs.getInt("idArticulo"),
            rs.getString("skuSicofi"),
            rs.getString("nombreArticulo"),
            rs.getString("descripcion"),
            rs.getString("unidadMedida"),
            rs.getString("marca")
    );

    private final RowMapper<ReglaAmenidadActualDto> reglaAmenidadActualMapper = (rs, rowNum) -> new ReglaAmenidadActualDto(
            rs.getInt("idArticulo"),
            rs.getString("nombreArticulo"),
            rs.getString("unidadMedida"),
            rs.getInt("cantidadBase"),
            rs.getInt("cantidadPorPersona")
    );

    private final RowMapper<DetallesUnidadFisica> detallesUnidadFisicaMapper = (rs, rowNum) -> new DetallesUnidadFisica(
            rs.getInt("idUnidadFisica"),
            rs.getString("numeroUnidadFisica"),
            rs.getInt("tipoUnidad"),
            rs.getString("nombreTipoUnidad"),
            rs.getInt("capacidadMaxima"),
            rs.getBoolean("disponible"),
            rs.getInt("idDesarrollo"),
            rs.getString("nombreDesarrollo"),
            rs.getObject("piso") != null ? rs.getInt("piso") : null,
            rs.getInt("idEstatus"),
            rs.getString("nombreEstatus"),
            rs.getObject("idPadre") != null ? rs.getInt("idPadre") : null,
            rs.getString("codigoSicofi"),
            rs.getString("centroCostoNombre")
    );

    // =========================================================================
    // MÉTODOS DE ESCRITURA (Write)
    // =========================================================================

    @Caching(evict = {
            @CacheEvict(value = "tipo_unidad_detalles", key = "#request.idTipoUnidad()", condition = "#request.idTipoUnidad() != null"),
            @CacheEvict(value = "detalle_tipo_unidad", key = "#request.idTipoUnidad()", condition = "#request.idTipoUnidad() != null"),
            @CacheEvict(value = "tipos_unidad_cards", allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = "tipo_unidad_caracteristicas", key = "#tipoUnidadId"),
            @CacheEvict(value = "detalle_tipo_unidad", key = "#tipoUnidadId")
    })
    public Optional<Integer> spResvGuardarCaracteristicasTipoUnidad(Integer tipoUnidadId, List<RelacionCaracteristicaRequest> caracteristicas, String usuario) {
        return spExecutor.querySingleLog("spResvGuardarCaracteristicasTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "Caracteristicas", JsonUtils.toJson(caracteristicas), "Usuario", usuario),
                scalarIntMapper, usuario, false, true);
    }

    @Caching(evict = {
            @CacheEvict(value = "tipo_unidad_imagenes", key = "#tipoUnidadId"),
            @CacheEvict(value = "detalle_tipo_unidad", key = "#tipoUnidadId"),
            @CacheEvict(value = "tipos_unidad_cards", allEntries = true)
    })
    public Optional<Integer> spResvGuardarImagenesTipoUnidad(Integer tipoUnidadId, List<ImagenRequest> imagenes, String usuario) {
        return spExecutor.querySingleLog("spResvGuardarImagenesTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "ImagenesJson", JsonUtils.toJson(imagenes), "Usuario", usuario),
                scalarIntMapper, usuario, false, true);
    }

    @Caching(evict = {
            @CacheEvict(value = "tipo_unidad_imagenes", key = "#tipoUnidadId"),
            @CacheEvict(value = "detalle_tipo_unidad", key = "#tipoUnidadId"),
            @CacheEvict(value = "tipos_unidad_cards", allEntries = true)
    })
    public void spResvEliminarImagenesTipoUnidad(Integer tipoUnidadId, List<EliminarImagenRequest> imagenesAEliminar, String usuario) {
        spExecutor.executeLog("spResvEliminarImagenesTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "ImagenesJson", JsonUtils.toJson(imagenesAEliminar)),
                usuario, false, true);
    }

    @Caching(evict = {
            @CacheEvict(value = "tipo_unidad_imagenes", key = "#tipoUnidadId"),
            @CacheEvict(value = "detalle_tipo_unidad", key = "#tipoUnidadId"),
            @CacheEvict(value = "tipos_unidad_cards", allEntries = true)
    })
    public void spResvCambiarImagenPortadaTipoUnidad(Integer tipoUnidadId, UUID nuevaPortadaUuid, String usuario) {
        spExecutor.executeLog("spResvCambiarImagenPortadaTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId,
                        "NuevaPortadaUuid", nuevaPortadaUuid.toString()
                ),
                usuario,
                false,
                false);
    }

    @Caching(evict = {
            @CacheEvict(value = "tipo_unidad_detalles", key = "#tipoUnidadId"),
            @CacheEvict(value = "tipo_unidad_imagenes", key = "#tipoUnidadId"),
            @CacheEvict(value = "tipo_unidad_caracteristicas", key = "#tipoUnidadId"),
            @CacheEvict(value = "detalle_tipo_unidad", key = "#tipoUnidadId"),
            @CacheEvict(value = "tipos_unidad_cards", allEntries = true)
    })
    public void spResvDesactivarTipoUnidad(Integer tipoUnidadId, String usuario) {
        spExecutor.executeLog("spResvDesactivarTipoUnidad",
                Map.of("TipoUnidadId", tipoUnidadId, "Usuario", usuario),
                usuario, true, true);
    }


    // =========================================================================
    // MÉTODOS DE LECTURA (Read)
    // =========================================================================

    @Cacheable(value = "tipos_unidad_cards", key = "#idDesarrollo != null ? #idDesarrollo : 'todos'")
    public List<TipoUnidadCardDto> spResvObtenerTiposUnidadCard(Integer idDesarrollo) {
        return spExecutor.queryList("spResvObtenerTiposUnidadCard", Map.of("IdDesarrollo", idDesarrollo), tipoUnidadCardMapper);
    }

    @Cacheable(value = "tipo_unidad_detalles", key = "#idTipoUnidad")
    public TipoUnidadDetalleDto spResvObtenerTipoUnidadDetalles(Integer idTipoUnidad) {
        return spExecutor.querySingle("spResvObtenerTipoUnidadDetalles", Map.of("IdTipoUnidad", idTipoUnidad), tipoUnidadDetalleMapper).orElse(null);
    }

    @Cacheable(value = "tipo_unidad_imagenes", key = "#idTipoUnidad")
    public List<ImagenDto> spResvObtenerTipoUnidadImagenes(Integer idTipoUnidad) {
        return spExecutor.queryList("spResvObtenerTipoUnidadImagenes", Map.of("IdTipoUnidad", idTipoUnidad), imagenUnidadMapper);
    }

    @Cacheable(value = "tipo_unidad_caracteristicas", key = "#idTipoUnidad")
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
        params.put("CentroCostoNombre", request.centroCostoNombre());
        params.put("CodigoSicofi", request.codigoSicofi());
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

    public void spResvDesactivarUnidadFisica(DesactivarUnidadRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdUnidadFisica", request.idUnidadFisica());
        params.put("FechaInicio", request.fechaInicio());
        params.put("FechaFin", request.fechaFin());
        params.put("RazonBloqueo", request.razonBloqueo());
        params.put("Usuario", usuario);

        spExecutor.executeLog("spResvDesactivarUnidadFisica",
                params,
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

    @Cacheable(value = "detalle_tipo_unidad", key = "#idTipoUnidad")
    public TipoUnidadDetalles spResvObtenerDetalleTipoUnidad(Integer idTipoUnidad) {
        return spExecutor.querySingle("spResvObtenerDetalleTipoUnidad", Map.of("RhdtId", idTipoUnidad), detallesUIRowMapper).orElse(null);
    }

    public List<UnidadBloqueadaDto> obtenerUnidadesBloqueadas(Integer idDesarrollo) {
        return spExecutor.queryList(
                "spResvObtenerUnidadesBloqueadas",
                Map.of("IdDesarrollo", idDesarrollo),
                unidadBloqueadaMapper
        );
    }

    public void reactivarUnidadFisica(Integer idUnidadFisica, String usuario) {
        spExecutor.execute(
                "spResvReactivarUnidadFisica",
                Map.of(
                        "IdUnidadFisica", idUnidadFisica,
                        "Usuario", usuario
                )
        );
    }

    public List<ArticuloAmenidadDto> obtenerCatalogoAmenidades() {
        return spExecutor.queryList(
                "spInvObtenerCatalogoAmenidades",
                Map.of(),
                articuloAmenidadMapper
        );
    }

    public List<ReglaAmenidadActualDto> obtenerReglasAmenidades(Integer rhdtId) {
        return spExecutor.queryList(
                "spAmaObtenerReglasAmenidades",
                Map.of("RhdtId", rhdtId),
                reglaAmenidadActualMapper
        );
    }

    public void guardarReglasAmenidades(Integer rhdtId, String jsonReglas, String usuario) {
        spExecutor.execute(
                "spAmaGuardarReglasAmenidades",
                Map.of(
                        "RhdtId", rhdtId,
                        "JsonReglas", jsonReglas,
                        "Usuario", usuario
                )
        );
    }

    public Optional<DetallesUnidadFisica> spResvObtenerDetallesUnidadFisica(Integer idUnidadFisica) {
        return spExecutor.querySingle(
                "spResvObtenerDetallesUnidadFisica",
                Map.of("IdUnidadFisica", idUnidadFisica),
                detallesUnidadFisicaMapper
        );
    }
}
