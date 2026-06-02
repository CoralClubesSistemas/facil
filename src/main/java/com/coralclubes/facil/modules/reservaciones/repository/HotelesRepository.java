package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.projection.*;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.shared.domain.dto.ImagenDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelDetalleDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HotelesRepository {

    private final StoredProcedureExecutor spExecutor;

    // =========================================================================
    // MAPPERS (Mapeo de ResultSets a Records)
    // =========================================================================

    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    private final RowMapper<HotelCardDto> hotelCardMapper = (rs, rowNum) -> HotelCardDto.builder()
            .idDesarrollo(rs.getInt("ID_DESARROLLO"))
            .nombreHotel(rs.getString("NOMBRE_HOTEL"))
            .direccionCompleta(rs.getString("DIRECCION_COMPLETA"))
            .telefono(rs.getString("TELEFONO"))
            .descripcionCorta(rs.getString("DESCRIPCION_CORTA"))
            .calificacion(rs.getBigDecimal("CALIFICACION"))
            .uuidPortada(rs.getString("UUID_PORTADA") != null ? UUID.fromString(rs.getString("UUID_PORTADA")) : null)
            .build();

    private final RowMapper<HotelDetalleDto> hotelDetalleMapper = (rs, rowNum) -> HotelDetalleDto.builder()
            .idDesarrollo(rs.getInt("ID_DESARROLLO"))
            .nombreHotel(rs.getString("NOMBRE_HOTEL"))
            .direccion(rs.getString("DIRECCION"))
            .numero(rs.getString("NUMERO"))
            .localidad(rs.getString("LOCALIDAD"))
            .ciudad(rs.getString("CIUDAD"))
            .estado(rs.getString("ESTADO"))
            .codigoPostal(rs.getString("CP"))
            .mapaIframe(rs.getString("MAPA_IFRAME"))
            .telefono(rs.getString("TELEFONO"))
            .descripcionCorta(rs.getString("DESCRIPCION_CORTA"))
            .descripcionLarga(rs.getString("DESCRIPCION_LARGA"))
            .calificacion(rs.getBigDecimal("CALIFICACION"))
            .direccionCompleta(rs.getString("DIRECCION_COMPLETA"))
            .build();

    private final RowMapper<ImagenDto> imagenHotelMapper = (rs, rowNum) -> ImagenDto.builder()
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
            .cantidad(rs.getInt("CANTIDAD"))
            .idTipo(rs.getObject("ID_TIPO") != null ? rs.getInt("ID_TIPO") : null)
            .build();


    // =========================================================================
    // MÉTODOS DE ESCRITURA (Write / Upsert / Delete)
    // =========================================================================

    public Optional<Integer> spResvGuardarHotel(HotelRequest hotel, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdHotel", hotel.id());
        params.put("NombreHotel", hotel.nombre());
        params.put("Direccion", hotel.direccion());
        params.put("Numero", hotel.numero());
        params.put("Localidad", hotel.localidad());
        params.put("Ciudad", hotel.ciudad());
        params.put("Estado", hotel.estado());
        params.put("CodigoPostal", hotel.codigoPostal());
        params.put("MapaIframe", hotel.mapaIframe());
        params.put("DescripcionCorta", hotel.descripcionCorta());
        params.put("DescripcionLarga", hotel.descripcionLarga());
        params.put("Telefono", hotel.telefono());
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvGuardarHotel", params, scalarIntMapper, usuario, true, true);
    }

    public Optional<Integer> spResvGuardarCaracteristicasHotel(Integer hotelId, List<RelacionCaracteristicaRequest> caracteristicas, String usuario) {
        // Convertimos la lista de Java a String JSON para que el SP pueda leerla con OPENJSON
        String jsonCaracteristicas = JsonUtils.toJson(caracteristicas);

        Map<String, Object> params = Map.of(
                "HotelId", hotelId,
                "Caracteristicas", jsonCaracteristicas,
                "Usuario", usuario
        );

        return spExecutor.querySingleLog("spResvGuardarCaracteristicasHotel", params, scalarIntMapper, usuario, false, true);
    }

    public Optional<Integer> spResvGuardarImagenesHotel(Integer hotelId, List<ImagenRequest> imagenes, String usuario) {
        String jsonImagenes = JsonUtils.toJson(imagenes);

        Map<String, Object> params = Map.of(
                "HotelId", hotelId,
                "ImagenesJson", jsonImagenes,
                "Usuario", usuario
        );

        return spExecutor.querySingleLog("spResvGuardarImagenesHotel", params, scalarIntMapper, usuario, false, true);
    }

    public void spResvEliminarImagenesHotel(Integer hotelId, List<EliminarImagenRequest> imagenes, String usuario) {
        String jsonImagenes = JsonUtils.toJson(imagenes);

        Map<String, Object> params = Map.of(
                "HotelId", hotelId,
                "ImagenesJson", jsonImagenes
        );

        spExecutor.executeLog("spResvEliminarImagenesHotel", params, usuario, false, true);
    }

    public void spResvCambiarImagenPortadaHotel(Integer hotelId, UUID nuevaPortadaUuid, String usuario) {
        Map<String, Object> params = Map.of(
                "HotelId", hotelId,
                "NuevaPortadaUuid", nuevaPortadaUuid.toString()
        );

        spExecutor.executeLog("spResvCambiarImagenPortadaHotel", params, usuario, false, false);
    }

    public void spResvDesactivarHotel(Integer hotelId, String usuario) {
        Map<String, Object> params = Map.of(
                "HotelId", hotelId,
                "Usuario", usuario
        );

        spExecutor.executeLog("spResvDesactivarHotel", params, usuario, true, true);
    }


    // =========================================================================
    // MÉTODOS DE LECTURA (Read)
    // =========================================================================

    public List<HotelCardDto> spResvObtenerHotelesCard(Integer idDesarrollo) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdDesarrollo", idDesarrollo); // Puede ser null

        return spExecutor.queryList("spResvObtenerHotelesCard", params, hotelCardMapper);
    }

    public Optional<HotelDetalleDto> spResvObtenerHotelDetalles(Integer idDesarrollo) {
        return spExecutor.querySingle("spResvObtenerHotelDetalles", Map.of("IdDesarrollo", idDesarrollo), hotelDetalleMapper);
    }

    public List<ImagenDto> spResvObtenerHotelImagenes(Integer idDesarrollo) {
        return spExecutor.queryList("spResvObtenerHotelImagenes", Map.of("IdDesarrollo", idDesarrollo), imagenHotelMapper);
    }

    public List<CaracteristicaDto> spResvObtenerCaracteristicasXHotel(Integer idHotel) {
        return spExecutor.queryList("spResvObtenerCaracteristicasXHotel", Map.of("IdHotel", idHotel), caracteristicaMapper);
    }
}