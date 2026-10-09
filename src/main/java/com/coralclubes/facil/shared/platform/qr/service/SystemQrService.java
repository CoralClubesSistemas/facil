package com.coralclubes.facil.shared.platform.qr.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDetalleProjection;
import com.coralclubes.facil.modules.sistema.service.ModulosService;
import com.coralclubes.facil.shared.domain.enums.ClavesModulos;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaLegacyDto;
import com.coralclubes.facil.shared.platform.qr.dto.CreateQrRequest;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResolucionResponse;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResponse;
import com.coralclubes.facil.shared.platform.qr.mapper.SystemQrMapper;
import com.coralclubes.facil.shared.platform.qr.model.SystemQr;
import com.coralclubes.facil.shared.platform.qr.repository.SystemQrRepository;
import com.coralclubes.facil.shared.utils.QrCodeService;
import com.coralclubes.facil.shared.utils.UuidUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio transversal para la creación, codificación y almacenamiento de códigos QR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemQrService {

    private final SystemQrRepository systemQrRepository;
    private final SystemQrMapper systemQrMapper;
    private final QrCodeService qrCodeService;
    private final StorageClient storageClient;
    private final ModulosService modulosService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DEFAULT_STORAGE_FOLDER = "qrs";

    /**
     * Crea un código QR generando un token criptográficamente seguro, generando la imagen física,
     * subiéndola al servicio de almacenamiento y persistiendo el registro en la base de datos.
     *
     * @param request Datos de la solicitud de creación del QR.
     * @return DTO de respuesta con los metadatos persistidos y la URL de descarga del archivo.
     */
    @Transactional
    public SystemQrResponse crearQr(CreateQrRequest request) {
        String token = generarTokenSeguro(request);
        byte[] qrBytes = qrCodeService.generarQr(token);

        // 1. Subir al servicio de almacenamiento
        InfoArchivoDto archivoSubido = subirAStorage(request, qrBytes);
        UUID qrFileId = archivoSubido.uuid();
        String urlDescarga = archivoSubido.urlDescarga();

        // 2. Persistir en la base de datos
        SystemQr entity = systemQrMapper.toEntity(request, token, qrFileId);
        SystemQr guardado = systemQrRepository.save(entity);

        log.info("Código QR creado y almacenado exitosamente. Modulo: {}, Acción: {}, Entidad: {}, Token: {}, UUID Archivo: {}",
                request.module(), request.actionType(), request.entityId(), token, qrFileId);
        return systemQrMapper.toResponse(guardado, urlDescarga);
    }

    /**
     * Método sobrecargado: Genera el código QR de forma segura, almacena el registro en la base de datos
     * y permite decidir si se persiste en el Storage, retornando siempre el archivo como flujo de bytes.
     *
     * @param request            Datos de la solicitud.
     * @param almacenarEnStorage Indica si el archivo físico debe cargarse al servicio de almacenamiento.
     * @return Flujo de bytes (PNG) del código QR generado.
     */
    @Transactional
    public byte[] crearQr(CreateQrRequest request, boolean almacenarEnStorage) {
        String token = generarTokenSeguro(request);
        byte[] qrBytes = qrCodeService.generarQr(token);

        UUID qrFileId = null;
        if (almacenarEnStorage) {
            InfoArchivoDto archivoSubido = subirAStorage(request, qrBytes);
            qrFileId = archivoSubido.uuid();
        }

        // Persistir en la base de datos (con o sin referencia a archivo en storage)
        SystemQr entity = systemQrMapper.toEntity(request, token, qrFileId);
        systemQrRepository.save(entity);

        log.info("Código QR creado. Modulo: {}, Acción: {}, Entidad: {}, Token: {}, UUID Archivo: {}",
                request.module(), request.actionType(), request.entityId(), token, qrFileId);
        return qrBytes;
    }

    /**
     * Sobrecarga de conveniencia: Genera el código QR, lo almacena en la base de datos
     * SIN almacenar en el Storage y devuelve el archivo como flujo de bytes.
     *
     * @param request Datos de la solicitud de creación del QR.
     * @return Flujo de bytes (PNG) del código QR generado.
     */
    @Transactional
    public byte[] crearQrSinStorage(CreateQrRequest request) {
        return crearQr(request, false);
    }

    /**
     * Busca un código QR activo por módulo, tipo de entidad, coincidencia exacta de ID o coincidencia por patrón.
     * Es ideal para entidades compuestas o listas de identificadores generadas por ejemplo en carritos de reservación.
     *
     * @param module        Módulo emisor (ej. 'RESERVACIONES').
     * @param entityType    Tipo de entidad (ej. 'RESERVATION').
     * @param exactEntityId Identificador exacto de la entidad (ej. 'MEMB-001:101').
     * @param pattern       Patrón SQL LIKE (ej. 'MEMB-001:%101%').
     * @return DTO de respuesta con los datos del QR si existe uno activo, u Optional vacío.
     */
    @Transactional(readOnly = true)
    public Optional<SystemQrResponse> obtenerQrActivoPorModuloYEntidad(
            String module,
            String entityType,
            String exactEntityId,
            String pattern
    ) {
        List<SystemQr> resultados = systemQrRepository.buscarActivosPorModuloEntidadYPatron(
                module, entityType, com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus.ACTIVE, exactEntityId, pattern
        );
        return resultados.stream().findFirst().map(systemQrMapper::toResponse);
    }

    /**
     * Resuelve un código QR a partir de su token público, obteniendo su información completa
     * y los datos del módulo/submódulo destino en el sistema a partir del SP spFacilObtenerModuloPorClave.
     *
     * @param qrToken Token público del QR.
     * @return DTO compuesto con la información del QR y del módulo resuelto.
     */
    @Transactional(readOnly = true)
    public SystemQrResolucionResponse resolverQrPorToken(String qrToken) {
        SystemQr qr = systemQrRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún código QR con el token especificado"));

        // Mapear módulo y submódulo a ClavesModulos
        String clavePadre = null;
        String claveModulo = null;

        var optModulo = ClavesModulos.desdeNombre(qr.getModule());
        var optSubmodulo = ClavesModulos.desdeNombre(qr.getSubmodule());

        if (optSubmodulo.isPresent()) {
            claveModulo = optSubmodulo.get().getClave();
            clavePadre = optModulo.map(ClavesModulos::getClave).orElse(null);
        } else if (optModulo.isPresent()) {
            claveModulo = optModulo.get().getClave();
            clavePadre = null;
        }

        String rutaModulo = null;
        if (claveModulo != null) {
            rutaModulo = modulosService.obtenerModuloPorClave(clavePadre, claveModulo)
                    .map(ModuloDetalleProjection::ruta)
                    .orElse(null);
        }

        return SystemQrResolucionResponse.builder()
                .id(qr.getId())
                .token(qr.getQrToken())
                .module(qr.getModule())
                .submodule(qr.getSubmodule())
                .actionType(qr.getActionType())
                .entityId(qr.getEntityId())
                .entityType(qr.getEntityType())
                .status(qr.getStatus())
                .metadata(qr.getMetadata())
                .rutaModulo(rutaModulo)
                .build();
    }

    // =========================================================================
    // MÉTODOS PRIVADOS AUXILIARES (HELPERS)
    // =========================================================================

    /**
     * Genera un token único y seguro codificando la información de la solicitud (módulo, tipo de acción,
     * entidad, timestamp y entropía criptográfica) mediante SHA-256.
     */
    private String generarTokenSeguro(CreateQrRequest request) {
        UUID seedUuid = UuidUtils.generateV7();
        long timestamp = System.currentTimeMillis();
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);

        String payload = String.format("%s:%s:%s:%s:%s:%d:%s",
                sanitizar(request.module()),
                sanitizar(request.actionType()),
                sanitizar(request.entityType()),
                sanitizar(request.entityId()),
                seedUuid,
                timestamp,
                HexFormat.of().formatHex(randomBytes)
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            // Token puramente alfanumérico resultante de la codificación (64 caracteres alfanuméricos)
            String token = HexFormat.of().formatHex(hash);

            // Garantizar unicidad contra la base de datos
            if (systemQrRepository.existsByQrToken(token)) {
                return generarTokenSeguro(request);
            }

            return token;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash no disponible para generación de token QR", e);
        }
    }

    /**
     * Realiza la carga síncrona del archivo de imagen QR al servicio de almacenamiento.
     */
    private InfoArchivoDto subirAStorage(CreateQrRequest request, byte[] qrBytes) {
        String moduloLimpio = sanitizar(request.module()).toLowerCase();
        String entidadLimpia = sanitizar(request.entityId()).replace(":", "_").replace("-", "");
        String nombreArchivo = String.format("QR_%s_%s_%d.png", moduloLimpio, entidadLimpia, System.currentTimeMillis());

        String rutaLogica = (request.urlAlmacenamiento() != null && !request.urlAlmacenamiento().isBlank())
                ? request.urlAlmacenamiento().trim()
                : DEFAULT_STORAGE_FOLDER + "/" + moduloLimpio;

        SolicitudCargaLegacyDto solicitudCarga = SolicitudCargaLegacyDto.builder()
                .idCorrelacion(request.module() + "_" + request.entityId() + "_" + System.currentTimeMillis())
                .esPublico(false)
                .rutaLogica(rutaLogica)
                .metadatos(Map.of(
                        "modulo", request.module(),
                        "actionType", request.actionType(),
                        "entityType", request.entityType(),
                        "entityId", request.entityId()
                ))
                .requiereDepuracion(false)
                .build();

        return storageClient.cargarArchivoSincrono(
                qrBytes,
                nombreArchivo,
                "image/png",
                solicitudCarga
        );
    }

    private String sanitizar(String valor) {
        return valor != null ? valor.trim().replaceAll("[^a-zA-Z0-9_-]", "_") : "DESCONOCIDO";
    }
}
