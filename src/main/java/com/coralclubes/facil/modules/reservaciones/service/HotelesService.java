package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.projection.*;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelCardUI;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelDetalleDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelesCardList;
import com.coralclubes.facil.shared.domain.dto.ImagenResponse;
import com.coralclubes.facil.modules.reservaciones.repository.HotelesRepository;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudDescargaBatchDto;
import com.coralclubes.facil.shared.domain.dto.ImagenDto;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio que maneja la lógica de negocio relacionada con los hoteles.
 * Orquesta las llamadas al repositorio y asegura la inyección del contexto de seguridad.
 */
@Service
@RequiredArgsConstructor
public class HotelesService {

    private final HotelesRepository repo;
    private final UserContext userContext;
    private final StorageClient storageClient;
    private final StorageUrlsCacheService storageUrlsCacheService;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorageDefault;

    // =========================================================================
    // MÉTODOS DE ESCRITURA (Write)
    // =========================================================================

    /**
     * Crea o actualiza la información general de un hotel.
     */
    public ApiResponse<Integer> guardarHotel(HotelRequest hotelRequest) {
        String usuario = userContext.getUsername();

        Integer idGenerado = repo.spResvGuardarHotel(hotelRequest, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("No se pudo procesar la creación/edición del hotel."));

        return ApiResponse.success("Hotel guardado correctamente", idGenerado);
    }

    /**
     * Sincroniza las características de un hotel (Delete y Re-insert).
     */
    public ApiResponse<Integer> guardarCaracteristicasHotel(GuardarCaracteristicasRequest request) {
        String usuario = userContext.getUsername();

        Integer insertadas = repo.spResvGuardarCaracteristicasHotel(request.id(), request.caracteristicas(), usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al sincronizar las características del hotel."));

        return ApiResponse.success("Características actualizadas con éxito (" + insertadas + " insertadas)", insertadas);
    }

    /**
     * Guarda el registro de nuevas imágenes en la base de datos (UUIDs del Storage).
     */
    public ApiResponse<Integer> guardarImagenesHotel(GuardarImagenesRequest request) {
        String usuario = userContext.getUsername();

        Integer insertadas = repo.spResvGuardarImagenesHotel(request.id(), request.imagenes(), usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al registrar las imágenes del hotel."));

        return ApiResponse.success("Imágenes registradas con éxito (" + insertadas + " agregadas)", insertadas);
    }

    /**
     * Elimina imágenes específicas de la galería del hotel.
     */
    @Transactional
    public ApiResponse<Boolean> eliminarImagenesHotel(EliminarImagenesRequest request) {
        String usuario = userContext.getUsername();
        repo.spResvEliminarImagenesHotel(request.id(), request.imagenesAEliminar(), usuario);

        // Solicitamos la eliminacion de las imágenes al microservicio de almacenamiento (de manera asíncrona, sin esperar la respuesta)
        request.imagenesAEliminar().forEach(imagen -> {
            if (imagen.uuid() != null) {
                // las eliminamos de forma permanente, no es necesario mantener una copia en la papelera porque el usuario ya confirmó que desea eliminarla
                storageClient.eliminarArchivo(imagen.uuid(), true);
            }
        });

        return ApiResponse.success("Imágenes eliminadas correctamente", true);
    }

    public ApiResponse<Boolean> cambiarImagenPortadaHotel(CambiarPortadaRequest request) {
        String usuario = userContext.getUsername();
        repo.spResvCambiarImagenPortadaHotel(request.id(), request.nuevaPortadaUuid(), usuario);

        return ApiResponse.success("Imagen de portada actualizada correctamente", true);
    }

    public ApiResponse<Boolean> desactivarHotel(Integer idHotel) {
        String usuario = userContext.getUsername();
        repo.spResvDesactivarHotel(idHotel, usuario);

        return ApiResponse.success("Hotel desactivado correctamente", true);
    }

    // =========================================================================
    // MÉTODOS DE LECTURA (Read)
    // =========================================================================

    /**
     * Obtiene la lista de hoteles activos en formato tarjeta.
     */
    public ApiResponse<List<HotelCardUI>> obtenerHotelesCard(Integer idDesarrollo) {
        List<HotelCardDto> hoteles = repo.spResvObtenerHotelesCard(idDesarrollo);

        if (hoteles.isEmpty()) {
            return ApiResponse.success("No se encontraron hoteles", new ArrayList<>());
        }

        List<HotelCardUI> hotelesUi = hoteles.stream().map(hotel -> {
            var uuidPortada = hotel.uuidPortada();
            String urlPortada = uuidPortada != null ? storageUrlsCacheService.obtenerUrlImagen(uuidPortada) : null;

            List<CaracteristicaDto> caracteristicas = repo.spResvObtenerCaracteristicasXHotel(hotel.idDesarrollo());

            return new HotelCardUI(
                    hotel.idDesarrollo(),
                    hotel.nombreHotel(),
                    hotel.direccionCompleta(),
                    hotel.telefono(),
                    hotel.descripcionCorta(),
                    hotel.calificacion(),
                    urlPortada,
                    caracteristicas
            );
        }).toList();

        return ApiResponse.success(hotelesUi);
    }

    /**
     * Obtiene el detalle completo de un hotel para el formulario de edición.
     */
    public ApiResponse<HotelDetalleDto> obtenerHotelDetalles(Integer idDesarrollo) {
        HotelDetalleDto detalle = repo.spResvObtenerHotelDetalles(idDesarrollo);

        if (detalle == null) {
            return ApiResponse.error(GeneralResponseCode.NOT_FOUND, "El hotel solicitado no existe o está inactivo.");
        }

        return ApiResponse.success(detalle);
    }

    /**
     * Obtiene la galería de imágenes (UUIDs y orden) de un hotel.
     */
    public ApiResponse<List<ImagenResponse>> obtenerHotelImagenes(Integer idDesarrollo) {
        List<ImagenDto> dbImgs = repo.spResvObtenerHotelImagenes(idDesarrollo);

        // extraemos el listado de uuids de las imagenes
        List<UUID> uuids = dbImgs.stream()
                .map(ImagenDto::uuid)
                .filter(uuid -> uuid != null)
                .toList();

        // construimos un map de uuid -> url
        Map<UUID, String> urlMap = new HashMap<>();
        if (!uuids.isEmpty()) {
            // ejecutamos una llamada batch para obtener todas las URLs de descarga en una sola consulta al microservicio de almacenamiento
            var batchResult = storageUrlsCacheService.consultarArchivosBatch(new SolicitudDescargaBatchDto(uuids));
            if (batchResult != null && batchResult.exitosos() != null) {
                for (var info : batchResult.exitosos()) {
                    if (info.uuid() != null && info.urlDescarga() != null) {
                        urlMap.put(info.uuid(), info.urlDescarga());
                    }
                }
            }
        }

        List<ImagenResponse> imgs = dbImgs.stream()
                .map(img -> ImagenResponse.builder()
                        .idImagen(img.idImagen())
                        .urlImagen(img.uuid() != null ? urlMap.get(img.uuid()) : null)
                        .uuid(img.uuid())
                        .esPortada(img.esPortada())
                        .orden(img.orden())
                        .build())
                .toList();

        return ApiResponse.success(imgs);
    }

    /**
     * Obtiene las características y amenidades asociadas a un hotel.
     */
    public ApiResponse<List<CaracteristicaDto>> obtenerCaracteristicasXHotel(Integer idHotel) {
        List<CaracteristicaDto> caracteristicas = repo.spResvObtenerCaracteristicasXHotel(idHotel);
        return ApiResponse.success(caracteristicas);
    }

    /**
     * Genera una URL presignada para que el Frontend suba una imagen de hotel.
     */
    public ApiResponse<RespuestaCargaDto> obtenerUrlCargaImagen(SolicitarUrlRequest request) {
        String usuario = userContext.getUsername();

        // 2. Construir la ruta lógica inmutable
        String rutaLogica = "reservaciones/hoteles/" + request.id();

        // 3. Crear la solicitud para el microservicio
        SolicitudCargaDto solicitudStorage = SolicitudCargaDto.builder()
                .nombreArchivo(request.nombreArchivo())
                .contentType(request.contentType())
                .tamanoBytes(request.tamanoBytes())
                .aliasConfiguracion(aliasStorageDefault)
                .esPublico(true)
                .rutaLogica(rutaLogica)
                .metadatos(Map.of(
                        "modulo", "HOTELES",
                        "idHotel", String.valueOf(request.id()),
                        "subidoPor", usuario
                ))
                .build();

        // 4. Solicitar la URL al microservicio de almacenamiento
        RespuestaCargaDto respuestaStorage = storageClient.solicitarUrlCarga(solicitudStorage);

        return ApiResponse.success("URL de carga generada exitosamente", respuestaStorage);
    }

    public List<HotelesCardList> obtenerHotelesDesactivados() {
        return repo.spResvObtenerHotelesDesactivados().stream()
                .map(hotel -> {
                    var uuidPortada = hotel.portada_uuid();
                    String urlPortada = uuidPortada != null ? storageUrlsCacheService.obtenerUrlImagen(uuidPortada) : null;

                    return HotelesCardList.builder()
                            .id(hotel.id())
                            .nombre(hotel.nombre())
                            .descripcion(hotel.descripcion())
                            .portada_uuid(hotel.portada_uuid())
                            .portada_url(urlPortada)
                            .build();
                }).toList();
    }

    public void reactivarHotel(Integer id) {
        repo.spResvReactivarHotel(id);
    }
}