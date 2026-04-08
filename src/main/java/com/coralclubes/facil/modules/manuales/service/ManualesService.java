package com.coralclubes.facil.modules.manuales.service;

import com.coralclubes.facil.modules.manuales.dto.request.ManualRequest;
import com.coralclubes.facil.modules.manuales.dto.request.VersionRequest;
import com.coralclubes.facil.modules.manuales.dto.response.ManualResponse;
import com.coralclubes.facil.modules.manuales.dto.response.VersionResponse;
import com.coralclubes.facil.modules.manuales.repository.ManualesRepository;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManualesService {

    private final ManualesRepository repository;
    private final StorageClient storageClient;
    private final BusinessLogger logger;
    private final UserContext userContext;
    private final BusinessLogger businessLogger;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorageDefault;

    public ApiResponse<List<ManualResponse>> listarManuales(Integer moduloPadreId, Integer moduloId, Integer numeroPagina) {
        List<ManualResponse> manuales = repository.obtenerManuales(moduloPadreId, moduloId, numeroPagina);
        return ApiResponse.success("Manuales obtenidos correctamente", manuales);
    }

    public ApiResponse<String> obtenerUrlDescarga(Integer manualId, Integer version) {
        UUID uuid = repository.obtenerUuidArchivo(manualId, version)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado para el manual y versión especificados"));

        String url = storageClient.obtenerUrlDescarga(uuid);
        return ApiResponse.success("URL de descarga generada", url);
    }

    public ApiResponse<Integer> guardarManual(ManualRequest request) {
        String usuario = userContext.getUsername();

        Integer id = repository.guardarManual(request, usuario)
                .orElseThrow(() -> new RuntimeException("Error al guardar el manual en BD"));

        businessLogger.info(usuario, "Guardando/Actualizado el manual {} en BD", id);

        return ApiResponse.success("Manual guardado correctamente", id);
    }

    public ApiResponse<Boolean> eliminarManual(Integer id) {
        String usuario = userContext.getUsername();

        // 1. Obtener los UUIDs y aplicar borrado lógico
        List<UUID> uuids = repository.eliminarManual(id, usuario);

        // 2. Eliminar físicamente los archivos de MinIO/S3
        uuids.forEach(uuid -> {
            try {
                storageClient.eliminarArchivo(uuid, true);
            } catch (Exception e) {
                logger.error("MANUALES_SERVICE", "No se pudo eliminar el archivo físico: " + uuid, e);
            }
        });

        return ApiResponse.success("Manual eliminado correctamente", true);
    }

    public ApiResponse<Integer> publicarVersion(VersionRequest request) {
        String usuario = userContext.getUsername();

        Integer id = repository.publicarVersion(request, usuario)
                .orElseThrow(() -> new RuntimeException("Error al publicar la versión en BD"));
        return ApiResponse.success("Versión publicada correctamente", id);
    }

    public ApiResponse<List<VersionResponse>> listarVersiones(Integer manualId) {
        return ApiResponse.success("Historial de versiones obtenido", repository.obtenerVersiones(manualId));
    }

    public ApiResponse<RespuestaCargaDto> solicitarUrlTemporal(SolicitarUrlRequest request) {
        String usuario = userContext.getUsername();

        // Construir la ruta lógica
        String rutaLogica = "manuales/" + request.id();

        // Crear payload para el StorageClient
        SolicitudCargaDto solicitudStorage = SolicitudCargaDto.builder()
                .nombreArchivo(request.nombreArchivo())
                .contentType(request.contentType())
                .tamanoBytes(request.tamanoBytes())
                .aliasConfiguracion(aliasStorageDefault)
                .esPublico(true)
                .rutaLogica(rutaLogica)
                .metadatos(Map.of(
                        "modulo", "MANUALES",
                        "idManual", String.valueOf(request.id()),
                        "subidoPor", usuario
                ))
                .build();

        RespuestaCargaDto respuestaStorage = storageClient.solicitarUrlCarga(solicitudStorage);

        return ApiResponse.success("URL de carga generada exitosamente", respuestaStorage);
    }
}