package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaBatchDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudDescargaBatchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio genérico para consulta y almacenamiento en caché de URLs de descarga del Storage.
 * Mapea la información bajo la caché "storage_urls" usando el UUID del archivo como llave.
 */
@Service
@RequiredArgsConstructor
public class StorageUrlsCacheService {

    private final StorageClient storageClient;
    private final CacheManager cacheManager;

    private static final String CACHE_NAME = "storage_urls";

    /**
     * Obtiene la URL de descarga de manera individual usando la caché.
     */
    @Cacheable(value = CACHE_NAME, key = "#uuid")
    public String obtenerUrlImagen(UUID uuid) {
        return storageClient.obtenerUrlDescarga(uuid);
    }

    /**
     * Obtiene los detalles de descarga por lote. 
     * Consulta la caché para cada UUID y únicamente solicita al Storage aquellos que no estén cacheados.
     */
    public RespuestaBatchDto<InfoArchivoDto> consultarArchivosBatch(SolicitudDescargaBatchDto batchDto) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        List<UUID> faltantes = new ArrayList<>();
        List<InfoArchivoDto> exitosos = new ArrayList<>();
        List<RespuestaBatchDto.ErrorDetalleDto> fallidos = new ArrayList<>();

        // 1. Buscar en la caché local/Redis los UUIDs solicitados
        for (UUID uuid : batchDto.archivos()) {
            if (uuid == null) continue;
            String urlCached = cache != null ? cache.get(uuid, String.class) : null;
            if (urlCached != null) {
                exitosos.add(new InfoArchivoDto(uuid, null, null, null, null, "DISPONIBLE", true, urlCached, null));
            } else {
                faltantes.add(uuid);
            }
        }

        // 2. Si hay fallas de caché, consultar por lote al Storage
        if (!faltantes.isEmpty()) {
            RespuestaBatchDto<InfoArchivoDto> respuestaStorage = storageClient.consultarArchivosBatch(new SolicitudDescargaBatchDto(faltantes));
            if (respuestaStorage != null) {
                if (respuestaStorage.exitosos() != null) {
                    for (InfoArchivoDto info : respuestaStorage.exitosos()) {
                        exitosos.add(info);
                        // 3. Almacenar el nuevo resultado en caché
                        if (cache != null && info.uuid() != null && info.urlDescarga() != null) {
                            cache.put(info.uuid(), info.urlDescarga());
                        }
                    }
                }
                if (respuestaStorage.fallidos() != null) {
                    fallidos.addAll(respuestaStorage.fallidos());
                }
            }
        }

        return new RespuestaBatchDto<>(exitosos, fallidos);
    }

    /**
     * Invalida de la caché un UUID específico.
     */
    @CacheEvict(value = CACHE_NAME, key = "#uuid")
    public void eliminarUrlDeCache(UUID uuid) {
    }

    /**
     * Vacía por completo la caché de URLs.
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void vaciarCache() {
    }
}
