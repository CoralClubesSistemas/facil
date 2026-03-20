package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reservaciones.dto.projection.TipoUnidadDetalles;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.modules.reservaciones.dto.projection.TipoUnidadCardDto;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.repository.UnidadesRepository;
import com.coralclubes.facil.shared.infrastructure.domain.dto.ImagenResponse;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlImagenRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio central para la gestión de Unidades (Lógicas y Físicas).
 * Orquesta las llamadas al repositorio y gestiona la integración con el microservicio de almacenamiento.
 */
@Service
@RequiredArgsConstructor
public class UnidadesService {

    private final UnidadesRepository tipoUnidadRepo;
    private final UserContext userContext;
    private final StorageClient storageClient;
    private final BusinessLogger businessLogger;
    private final ObjectMapper objectMapper;
    private final AmaDeLlavesService amaDeLlavesService;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorageDefault;

    // =========================================================================
    // MÉTODOS DE ESCRITURA (Write) - TIPOS DE UNIDAD
    // =========================================================================

    public ApiResponse<Integer> guardarTipoUnidad(TipoUnidadRequest request) {
        String usuario = userContext.getUsername();

        Integer idGenerado = tipoUnidadRepo.spResvGuardarTipoUnidad(request, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("No se pudo procesar la creación/edición del tipo de unidad."));

        return ApiResponse.success("Tipo de unidad guardado correctamente", idGenerado);
    }

    public ApiResponse<Integer> guardarCaracteristicasTipoUnidad(GuardarCaracteristicasRequest request) {
        String usuario = userContext.getUsername();

        Integer insertadas = tipoUnidadRepo.spResvGuardarCaracteristicasTipoUnidad(request.id(), request.caracteristicas(), usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al sincronizar las características."));

        return ApiResponse.success("Características actualizadas con éxito (" + insertadas + " insertadas)", insertadas);
    }

    public ApiResponse<Integer> guardarImagenesTipoUnidad(GuardarImagenesRequest request) {
        String usuario = userContext.getUsername();

        Integer insertadas = tipoUnidadRepo.spResvGuardarImagenesTipoUnidad(request.id(), request.imagenes(), usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al registrar las imágenes."));

        return ApiResponse.success("Imágenes registradas con éxito", insertadas);
    }

    public ApiResponse<Boolean> eliminarImagenesTipoUnidad(EliminarImagenesRequest request) {
        String usuario = userContext.getUsername();
        boolean eliminado = tipoUnidadRepo.spResvEliminarImagenesTipoUnidad(request.id(), request.imagenesAEliminar(), usuario);

        // Solicitamos la eliminacion de las imágenes al microservicio de almacenamiento (de manera asíncrona, sin esperar la respuesta)
        request.imagenesAEliminar().forEach(imagen -> {
            if (imagen.uuid() != null) {
                // las eliminamos de forma permanente, no es necesario mantener una copia en la papelera porque el usuario ya confirmó que desea eliminarla
                storageClient.eliminarArchivo(imagen.uuid(), true);
            }
        });

        if (!eliminado) {
            return ApiResponse.error(GeneralResponseCode.INTERNAL_SERVER_ERROR, "No se pudieron eliminar las imágenes seleccionadas.");
        }
        return ApiResponse.success("Imágenes eliminadas correctamente", true);
    }

    public ApiResponse<Boolean> cambiarImagenPortadaTipoUnidad(CambiarPortadaRequest request) {
        String usuario = userContext.getUsername();
        boolean actualizado = tipoUnidadRepo.spResvCambiarImagenPortadaTipoUnidad(request.id(), request.nuevaPortadaUuid(), usuario);

        if (!actualizado) {
            return ApiResponse.error(GeneralResponseCode.INTERNAL_SERVER_ERROR, "No se pudo actualizar la portada.");
        }
        return ApiResponse.success("Portada actualizada correctamente", true);
    }

    public ApiResponse<Boolean> desactivarTipoUnidad(Integer idTipoUnidad) {
        String usuario = userContext.getUsername();
        boolean desactivado = tipoUnidadRepo.spResvDesactivarTipoUnidad(idTipoUnidad, usuario);

        if (!desactivado) {
            return ApiResponse.error(GeneralResponseCode.INTERNAL_SERVER_ERROR, "No se pudo desactivar el tipo de unidad.");
        }
        return ApiResponse.success("Tipo de unidad desactivado correctamente", true);
    }

    // =========================================================================
    // MÉTODOS DE LECTURA (Read) - TIPOS DE UNIDAD
    // =========================================================================

    public ApiResponse<List<TipoUnidadUI>> obtenerTiposUnidadCard() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        List<TipoUnidadCardDto> lista = tipoUnidadRepo.spResvObtenerTiposUnidadCard(idDesarrollo);

        List<TipoUnidadUI> ui = lista.stream().map(unidad -> {
            var uuidPortada = unidad.uuidPortada();
            String urlPortada = uuidPortada != null ? storageClient.obtenerUrlDescarga(uuidPortada) : null;

            List<CaracteristicaDto> caracteristicas = tipoUnidadRepo.spResvObtenerCaracteristicasXTipoUnidad(unidad.idTipoUnidad());

            return TipoUnidadUI.builder()
                    .idTipoUnidad(unidad.idTipoUnidad())
                    .idLsvTipoUnidad(unidad.idLsvTipoUnidad())
                    .nombreTipoUnidad(unidad.nombreTipoUnidad())
                    .capacidad(unidad.capacidad())
                    .descripcionCorta(unidad.descripcionCorta())
                    .urlImagen(urlPortada)
                    .calificacion(unidad.calificacion())
                    .caracteristicas(caracteristicas)
                    .idDesarrollo(unidad.idDesarrollo())
                    .nombreHotel(unidad.nombreHotel())
                    .build();
        }).toList();

        return ApiResponse.success(ui);
    }

    public ApiResponse<TipoUnidadDetalleDto> obtenerTipoUnidadDetalles(Integer idTipoUnidad) {
        TipoUnidadDetalleDto detalle = tipoUnidadRepo.spResvObtenerTipoUnidadDetalles(idTipoUnidad)
                .orElse(null);

        if (detalle == null) {
            return ApiResponse.error(GeneralResponseCode.NOT_FOUND, "El tipo de unidad no existe.");
        }
        return ApiResponse.success(detalle);
    }

    /**
     * Obtiene la galería de imágenes (UUIDs y orden) de un tipo de unidad.
     */
    public ApiResponse<List<ImagenResponse>> obtenerTipoUnidadImagenes(Integer idTipoUnidad) {
        List<ImagenResponse> imagenes = tipoUnidadRepo.spResvObtenerTipoUnidadImagenes(idTipoUnidad).stream()
                .map(img -> ImagenResponse.builder()
                        .idImagen(img.idImagen())
                        .urlImagen(img.uuid() != null ? storageClient.obtenerUrlDescarga(img.uuid()) : null)
                        .uuid(img.uuid())
                        .esPortada(img.esPortada())
                        .orden(img.orden())
                        .build())
                .toList();

        return ApiResponse.success(imagenes);
    }

    public ApiResponse<List<CaracteristicaDto>> obtenerCaracteristicasXTipoUnidad(Integer idTipoUnidad) {
        List<CaracteristicaDto> caracteristicas = tipoUnidadRepo.spResvObtenerCaracteristicasXTipoUnidad(idTipoUnidad);
        return ApiResponse.success(caracteristicas);
    }

    // =========================================================================
    // INTEGRACIÓN CON MICROSERVICIO DE ALMACENAMIENTO
    // =========================================================================

    /**
     * Negocia una URL presignada con Coral Almacenamiento para subir fotos de la habitación.
     */
    public ApiResponse<RespuestaCargaDto> obtenerUrlCargaImagen(SolicitarUrlImagenRequest request) {
        String usuario = userContext.getUsername();

        // 1. Validar que el Tipo de Unidad exista
        tipoUnidadRepo.spResvObtenerTipoUnidadDetalles(request.id())
                .orElseThrow(() -> new ServiceUnavailableException("El tipo de unidad especificado no existe o está inactivo."));

        // 2. Construir la ruta lógica
        String rutaLogica = "reservaciones/tipos-unidad/" + request.id();

        // 3. Crear payload para el StorageClient
        SolicitudCargaDto solicitudStorage = SolicitudCargaDto.builder()
                .nombreArchivo(request.nombreArchivo())
                .contentType(request.contentType())
                .tamanoBytes(request.tamanoBytes())
                .aliasConfiguracion(aliasStorageDefault)
                .esPublico(true)
                .rutaLogica(rutaLogica)
                .metadatos(Map.of(
                        "modulo", "TIPOS_UNIDAD",
                        "idTipoUnidad", String.valueOf(request.id()),
                        "subidoPor", usuario
                ))
                .build();

        // 4. Obtener URL del microservicio
        RespuestaCargaDto respuestaStorage = storageClient.solicitarUrlCarga(solicitudStorage);

        return ApiResponse.success("URL de carga generada exitosamente", respuestaStorage);
    }

    public ApiResponse<Integer> guardarUnidadFisica(UnidadFisicaRequest request) {
        String usuario = userContext.getUsername();
        Integer idGenerado = tipoUnidadRepo.spResvGuardarUnidadFisica(request, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("No se pudo guardar la unidad física."));
        return ApiResponse.success("Unidad física guardada correctamente", idGenerado);
    }

    public ApiResponse<Integer> asignarUnidadesFisicas(AsignarUnidadesFisicasRequest request) {
        String usuario = userContext.getUsername();
        Integer asignadas = tipoUnidadRepo.spResvAsignarUnidadesFisicasATipo(request.idTipoUnidad(), request.idsUnidadesFisicas(), usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al asignar las unidades físicas."));
        return ApiResponse.success("Se asignaron " + asignadas + " unidades correctamente", asignadas);
    }

    public ApiResponse<Integer> desasignarUnidadesFisicas(DesasignarUnidadesFisicasRequest request) {
        String usuario = userContext.getUsername();
        Integer desasignadas = tipoUnidadRepo.spResvDesasignarUnidadesFisicas(request.idsUnidadesFisicas(), usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al desasignar las unidades físicas."));
        return ApiResponse.success("Se liberaron " + desasignadas + " unidades correctamente", desasignadas);
    }

    public ApiResponse<Boolean> desactivarUnidadFisica(DesactivarUnidadRequest request) {
        String usuario = userContext.getUsername();
        boolean desactivado = tipoUnidadRepo.spResvDesactivarUnidadFisica(request, usuario);
        if (!desactivado) {
            return ApiResponse.error(GeneralResponseCode.INTERNAL_SERVER_ERROR, "No se pudo dar de baja la unidad.");
        }
        return ApiResponse.success("Unidad física dada de baja correctamente", true);
    }

    public ApiResponse<List<UnidadFisicaDto>> obtenerUnidadesFisicasAsignadas(Integer idTipoUnidad) {
        return ApiResponse.success(tipoUnidadRepo.spResvObtenerUnidadesFisicasXTipo(idTipoUnidad));
    }

    public ApiResponse<List<UnidadFisicaDto>> obtenerUnidadesFisicasDisponibles(Integer idDesarrollo) {
        return ApiResponse.success(tipoUnidadRepo.spResvObtenerUnidadesFisicasDisponiblesXDesarrollo(idDesarrollo));
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoPadres(Integer idDesarrollo, Integer idUnidadExcluida) {
        return ApiResponse.success(tipoUnidadRepo.spResvObtenerCatalogoPosiblesPadres(idDesarrollo, idUnidadExcluida));
    }

    public ApiResponse<TipoUnidadUIDetalles> obtenerTipoUnidadUIDetalles(Integer idTipoUnidad) {
        TipoUnidadDetalles detalles = tipoUnidadRepo.spResvObtenerDetalleTipoUnidad(idTipoUnidad)
                .orElseThrow(() -> new ServiceUnavailableException("No se pudieron obtener los detalles del tipo de unidad."));

        TipoUnidadUIDetalles uiDetalles = TipoUnidadUIDetalles.builder()
                .rhdtId(detalles.rhdtId())
                .nombreTipoUnidad(detalles.nombreTipoUnidad())
                .descripcionCorta(detalles.descripcionCorta())
                .descripcionLarga(detalles.descripcionLarga())
                .capacidad(detalles.capacidad())
                .calificacion(detalles.calificacion())
                .nombreDesarrollo(detalles.nombreDesarrollo())
                .caracteristicas(detalles.caracteristicas())
                .imagenes(detalles.imagenesUUID().stream().map(img -> ImagenResponse.builder()
                        .idImagen(img.idImagen())
                        .urlImagen(img.uuid() != null ? storageClient.obtenerUrlDescarga(img.uuid()) : null)
                        .uuid(img.uuid())
                        .esPortada(img.esPortada())
                        .orden(img.orden())
                        .build()).toList())
                .build();

        return ApiResponse.success(uiDetalles);
    }

    public ApiResponse<List<UnidadBloqueadaDto>> obtenerUnidadesBloqueadas() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        List<UnidadBloqueadaDto> bloqueadas = tipoUnidadRepo.obtenerUnidadesBloqueadas(idDesarrollo);
        return ApiResponse.success("Unidades bloqueadas obtenidas exitosamente", bloqueadas);
    }

    public ApiResponse<Boolean> reactivarUnidadFisica(ReactivarUnidadRequest request) {
        String usuario = userContext.getUsername();

        // Ejecutar SP de Reactivación en DB (que la pasa a estatus Sucia)
        tipoUnidadRepo.reactivarUnidadFisica(request.idUnidadFisica(), usuario);
        // Obtenemos el número de habitación para la creación de la tarea
        DetallesUnidadFisica numeroHabitacion = tipoUnidadRepo.spResvObtenerDetallesUnidadFisica(request.idUnidadFisica())
                .orElseThrow(() -> new ServiceUnavailableException("No se pudieron obtener los detalles de la unidad física."));

        // Disparar creación de tarea y WebSocket
        amaDeLlavesService.crearTareaYNotificar(
                request.idUnidadFisica(),
                numeroHabitacion.numeroUnidadFisica(),
                userContext.getIdDesarrollo(),
                usuario,
                "REACTIVACION_UNIDAD_FISICA"
        );

        return ApiResponse.success("La habitación ha sido reactivada y enviada a limpieza (Estatus: SUCIA).", true);
    }

    public ApiResponse<List<ArticuloAmenidadDto>> obtenerCatalogoAmenidades() {
        List<ArticuloAmenidadDto> catalogo = tipoUnidadRepo.obtenerCatalogoAmenidades();
        return ApiResponse.success("Catálogo de amenidades obtenido exitosamente", catalogo);
    }

    public ApiResponse<List<ReglaAmenidadActualDto>> obtenerReglasAmenidades(Integer rhdtId) {
        List<ReglaAmenidadActualDto> reglas = tipoUnidadRepo.obtenerReglasAmenidades(rhdtId);
        return ApiResponse.success("Reglas actuales obtenidas", reglas);
    }

    public ApiResponse<Boolean> guardarReglasAmenidades(GuardarAmenidadesRequest request) {
        try {
            String usuario = userContext.getUsername();

            // Convertimos la lista de objetos Java a un String JSON
            String jsonReglas = objectMapper.writeValueAsString(request.reglas());

            // Mandamos a SQL Server
            tipoUnidadRepo.guardarReglasAmenidades(request.rhdtId(), jsonReglas, usuario);

            businessLogger.info(usuario, "Reglas de amenidades actualizadas para el Tipo Unidad ID: {}", request.rhdtId());

            return ApiResponse.success("La configuración de amenidades se ha guardado correctamente.", true);

        } catch (JsonProcessingException e) {
            businessLogger.error("SYSTEM", "Error al parsear reglas de amenidades a JSON");
            return ApiResponse.error(GeneralResponseCode.BAD_REQUEST, "Error interno al procesar la lista de amenidades.");
        }
    }
}