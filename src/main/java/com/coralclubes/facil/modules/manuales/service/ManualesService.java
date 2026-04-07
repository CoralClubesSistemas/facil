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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManualesService {

    private final ManualesRepository repository;
    private final StorageClient storageClient;
    private final BusinessLogger logger;
    private final UserContext userContext;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorageDefault;

    public ApiResponse<List<ManualResponse>> listarManuales(Integer moduloId) {
        List<ManualResponse> manuales = repository.obtenerManuales(moduloId);
        
        // Generamos URLs de descarga para los que tienen UUID
        List<ManualResponse> manualesConUrl = manuales.stream()
                .map(m -> {
                    if (m.archivoUuid() != null) {
                        String url = storageClient.obtenerUrlDescarga(m.archivoUuid());
                        return ManualResponse.builder()
                                .id(m.id())
                                .nombre(m.nombre())
                                .descripcion(m.descripcion())
                                .moduloId(m.moduloId())
                                .moduloNombre(m.moduloNombre())
                                .versionId(m.versionId())
                                .version(m.version())
                                .archivoUuid(m.archivoUuid())
                                .nombreArchivo(m.nombreArchivo())
                                .tipo(m.tipo())
                                .urlDescarga(url)
                                .build();
                    }
                    return m;
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Manuales obtenidos correctamente", manualesConUrl);
    }

    public ApiResponse<Integer> guardarManual(ManualRequest request) {
        String usuario = userContext.getUsername();

        Integer id = repository.guardarManual(request, usuario)
                .orElseThrow(() -> new RuntimeException("Error al guardar el manual en BD"));
        return ApiResponse.success("Manual guardado correctamente", id);
    }

    public ApiResponse<Boolean> eliminarManual(Integer id) {
        // 1. Obtener los UUIDs de todos los archivos asociados a este manual antes de borrarlo lógicamente
        List<UUID> uuids = repository.eliminarManual(id);

        // 2. Eliminar físicamente los archivos de MinIO/S3
        uuids.forEach(uuid -> {
            try {
                storageClient.eliminarArchivo(uuid, true);
                logger.info("MANUALES_SERVICE", "Archivo eliminado físicamente: " + uuid);
            } catch (Exception e) {
                logger.error("MANUALES_SERVICE", "No se pudo eliminar el archivo físico: " + uuid, e);
            }
        });

        return ApiResponse.success("Manual eliminado correctamente", true);
    }

    public ApiResponse<Integer> publicarVersion(VersionRequest request) {
        // la versión anterior NO se elimina de S3 para mantener el historial,
        // a menos que el usuario lo pida explícitamente
        Integer id = repository.publicarVersion(request)
                .orElseThrow(() -> new RuntimeException("Error al publicar la versión en BD"));
        return ApiResponse.success("Versión publicada correctamente", id);
    }

    public ApiResponse<List<VersionResponse>> listarVersiones(Integer manualId) {
        return ApiResponse.success("Historial de versiones obtenido", repository.obtenerVersiones(manualId));
    }

    public ApiResponse<RespuestaCargaDto> solicitarUrlTemporal(SolicitarUrlRequest request) {
        String usuario = userContext.getUsername();

        // 2. Construir la ruta lógica
        String rutaLogica = "manuales/" + request.id() + "/" + request.nombreArchivo();

        // 3. Crear payload para el StorageClient
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

        // 4. Obtener URL del microservicio
        RespuestaCargaDto respuestaStorage = storageClient.solicitarUrlCarga(solicitudStorage);

        return ApiResponse.success("URL de carga generada exitosamente", respuestaStorage);
    }
}
