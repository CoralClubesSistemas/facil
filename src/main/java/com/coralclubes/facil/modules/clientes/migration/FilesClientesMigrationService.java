package com.coralclubes.facil.modules.clientes.migration;

import com.coralclubes.facil.modules.clientes.repository.NotasClientesRepository;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaLegacyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio encargado de la migración de archivos e imágenes almacenados en Base64
 * hacia el microservicio de almacenamiento (Coral Storage) para el módulo de clientes.
 * Procesa las notas de clientes y las credenciales de socios.
 */
@Service
@Slf4j
public class FilesClientesMigrationService {

    private final JdbcTemplate jdbcTemplate;
    private final StorageClient storageClient;
    private final NotasClientesRepository repository;
    private final String aliasStorageDefault;

    public FilesClientesMigrationService(
            JdbcTemplate jdbcTemplate,
            @Qualifier("migrationStorageClient") StorageClient storageClient,
            NotasClientesRepository repository,
            @Value("${app.clients.storage.aliases.default}") String aliasStorageDefault) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageClient = storageClient;
        this.repository = repository;
        this.aliasStorageDefault = aliasStorageDefault;
    }

    /**
     * Ejecuta el proceso de migración de forma asíncrona (en segundo plano).
     */
    @Async
    public void ejecutarMigracionAsync() {
        log.info("Iniciando migración de archivos de clientes en segundo plano (asíncrono)...");
        ejecutarMigracion();
    }

    /**
     * Ejecuta el proceso de migración de forma síncrona para todas las entidades soportadas.
     */
    public void ejecutarMigracion() {
        log.info("=== INICIANDO PROCESO GLOBAL DE MIGRACIÓN DE ARCHIVOS A CORAL STORAGE ===");

        try {
            migrarNotasClientes();
        } catch (Exception e) {
            log.error("Error crítico durante la migración de notas de clientes", e);
        }

        try {
            migrarCredencialesSocios();
        } catch (Exception e) {
            log.error("Error crítico durante la migración de credenciales de socios", e);
        }

        log.info("=== PROCESO GLOBAL DE MIGRACIÓN FINALIZADO ===");
    }

    /**
     * Realiza la migración de imágenes de la tabla IMAGENES_NOTAS_CLIENTES a la tabla ADJUNTOS_NOTAS_CLIENTES.
     */
    private void migrarNotasClientes() {
        log.info("--- Iniciando migración de Notas de Clientes ---");

        String query = "SELECT " +
                "  img.IMGNOT_NTC_MEM_MEMBRESIA AS membresia, " +
                "  img.IMGNOT_NTC_CONSECUTIVO AS consecutivo, " +
                "  img.IMGNOT_LSV_TIPOS_DOCUMENTOS AS tipoDocumento, " +
                "  img.IMGNOT_NUMERO_ORDEN_IMAGEN AS orden, " +
                "  img.IMGNOT_NOMBRE_ARCHIVO_FOTO AS nombreArchivo, " +
                "  img.IMGNOT_PATH_NOMBRE_FOTO_IMAGEN AS contenidoBase64, " +
                "  img.IMGNOT_USR_USUARIO AS usuario " +
                "FROM IMAGENES_NOTAS_CLIENTES img " +
                "WHERE img.IMGNOT_PATH_NOMBRE_FOTO_IMAGEN IS NOT NULL " +
                "  AND img.IMGNOT_PATH_NOMBRE_FOTO_IMAGEN <> '' " +
                "  AND NOT EXISTS ( " +
                "    SELECT 1 FROM ADJUNTOS_NOTAS_CLIENTES adj " +
                "    WHERE adj.ANC_NTC_MEM_MEMBRESIA = img.IMGNOT_NTC_MEM_MEMBRESIA " +
                "      AND adj.ANC_NTC_CONSECUTIVO = img.IMGNOT_NTC_CONSECUTIVO " +
                "      AND adj.ANC_NOMBRE_ARCHIVO = img.IMGNOT_NOMBRE_ARCHIVO_FOTO " +
                "  )";

        List<Map<String, Object>> records;
        try {
            records = jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            log.error("Error al consultar la tabla IMAGENES_NOTAS_CLIENTES. ¿Existe la tabla en este entorno?", e);
            return;
        }

        log.info("Registros de notas elegibles encontrados: {}", records.size());

        int exitos = 0;
        int fallos = 0;

        for (Map<String, Object> record : records) {
            String membresia = (String) record.get("membresia");
            Integer consecutivo = (Integer) record.get("consecutivo");
            String nombreArchivo = (String) record.get("nombreArchivo");
            String contenidoBase64 = (String) record.get("contenidoBase64");
            String usuario = (String) record.get("usuario");
            Integer orden = (Integer) record.get("orden");

            log.info("Procesando nota: Membresía={}, Consecutivo={}, Archivo='{}'", membresia, consecutivo, nombreArchivo);

            try {
                byte[] fileBytes = decodeBase64(contenidoBase64);
                if (fileBytes == null || fileBytes.length == 0) {
                    log.warn("Contenido decodificado vacío para Membresía {}, Consecutivo {}. Saltando.", membresia, consecutivo);
                    fallos++;
                    continue;
                }

                String contentType = determinarMimeType(nombreArchivo);
                String rutaLogica = "notas/archivos-socios/" + membresia + "/" + consecutivo;

                SolicitudCargaLegacyDto solicitud = SolicitudCargaLegacyDto.builder()
                        .idCorrelacion(membresia + "-" + consecutivo + "-" + orden)
                        .aliasConfiguracion(aliasStorageDefault)
                        .metadatos(Map.of(
                                "modulo", "CLIENTES",
                                "membresia", membresia,
                                "notaConsecutivo", String.valueOf(consecutivo),
                                "subidoPor", usuario != null ? usuario : "MIGRACION"
                        ))
                        .esPublico(false)
                        .rutaLogica(rutaLogica)
                        .requiereDepuracion(true)
                        .build();

                InfoArchivoDto response = storageClient.cargarArchivoSincrono(fileBytes, nombreArchivo, contentType, solicitud);
                UUID uuid = response.uuid();

                if (uuid == null) {
                    throw new IllegalStateException("El servicio de almacenamiento no retornó un UUID válido.");
                }

                repository.spRegistrarArhivosNotas(
                        membresia,
                        consecutivo,
                        nombreArchivo,
                        uuid.toString(),
                        contentType,
                        usuario != null ? usuario : "MIGRACION"
                );

                log.info("Migración de nota exitosa para: Membresía={}, Consecutivo={}, UUID={}", membresia, consecutivo, uuid);
                exitos++;
            } catch (Exception e) {
                log.error("Error al migrar nota para Membresía: {}, Consecutivo: {}. Detalle: {}", membresia, consecutivo, e.getMessage(), e);
                fallos++;
            }
        }

        log.info("Migración de notas finalizada. Éxitos: {}, Fallos: {}", exitos, fallos);
    }

    /**
     * Realiza la migración de imágenes de la tabla CREDENCIALES_SOCIOS a Coral Storage, actualizando su UUID.
     */
    private void migrarCredencialesSocios() {
        log.info("--- Iniciando migración de Credenciales de Socios ---");

        String query = "SELECT " +
                "  crd.CRD_MEM_MEMBRESIA AS membresia, " +
                "  crd.CRD_NUMERO_CREDENCIAL_ID AS credencialId, " +
                "  crd.CRD_BEN_NUMBENEFICIARIO AS beneficiario, " +
                "  crd.CRD_AÑO_VIGENCIA AS anioVigencia, " +
                "  crd.CRD_FOTO AS foto, " +
                "  crd.CRD_PATHFOTO AS contenidoBase64, " +
                "  crd.CRD_USR_USUARIO AS usuario " +
                "FROM CREDENCIALES_SOCIOS crd " +
                "WHERE crd.CRD_PATHFOTO IS NOT NULL " +
                "  AND crd.CRD_PATHFOTO <> '' " +
                "  AND crd.CRD_UUID_CREDENCIAL IS NULL " +
                "  AND crd.CRD_AÑO_VIGENCIA >= YEAR(GETDATE())";

        List<Map<String, Object>> records;
        try {
            records = jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            log.error("Error al consultar la tabla CREDENCIALES_SOCIOS. ¿Existe la tabla en este entorno?", e);
            return;
        }

        log.info("Registros de credenciales elegibles encontrados: {}", records.size());

        int exitos = 0;
        int fallos = 0;

        for (Map<String, Object> record : records) {
            String membresia = (String) record.get("membresia");
            String credencialId = (String) record.get("credencialId");
            Integer beneficiario = (Integer) record.get("beneficiario");
            Integer anioVigencia = (Integer) record.get("anioVigencia");
            String foto = (String) record.get("foto");
            String contenidoBase64 = (String) record.get("contenidoBase64");
            String usuario = (String) record.get("usuario");

            log.info("Procesando credencial: Membresía={}, CredencialId='{}', Beneficiario={}, Año={}",
                    membresia, credencialId, beneficiario, anioVigencia);

            try {
                byte[] fileBytes = decodeBase64(contenidoBase64);
                if (fileBytes == null || fileBytes.length == 0) {
                    log.warn("Contenido decodificado vacío para Membresía {}, CredencialId {}. Saltando.", membresia, credencialId);
                    fallos++;
                    continue;
                }

                // Determinar tipo MIME y Extensión
                String contentType;
                String extension;
                if (foto != null && !foto.trim().isEmpty()) {
                    contentType = determinarMimeType(foto);
                    int dotIdx = foto.lastIndexOf('.');
                    extension = (dotIdx != -1) ? foto.substring(dotIdx) : determinarExtensionPorMimeType(contentType);
                } else {
                    contentType = determinarMimeTypePorBytes(fileBytes);
                    extension = determinarExtensionPorMimeType(contentType);
                }

                // Generar nombre de archivo si no existe
                String nombreArchivo = (foto != null && !foto.trim().isEmpty()) ? foto.trim() : (credencialId + extension);

                // Ruta lógica para credenciales de socios
                String rutaLogica = "socios/credenciales/" + membresia + "/" + beneficiario;

                SolicitudCargaLegacyDto solicitud = SolicitudCargaLegacyDto.builder()
                        .idCorrelacion(membresia + "-" + credencialId + "-" + beneficiario + "-" + anioVigencia)
                        .aliasConfiguracion(aliasStorageDefault)
                        .metadatos(Map.of(
                                "modulo", "CLIENTES",
                                "membresia", membresia,
                                "credencialId", credencialId,
                                "beneficiarioId", String.valueOf(beneficiario),
                                "anioVigencia", String.valueOf(anioVigencia),
                                "subidoPor", usuario != null ? usuario : "MIGRACION"
                        ))
                        .esPublico(false)
                        .rutaLogica(rutaLogica)
                        .requiereDepuracion(true)
                        .build();

                InfoArchivoDto response = storageClient.cargarArchivoSincrono(fileBytes, nombreArchivo, contentType, solicitud);
                UUID uuid = response.uuid();

                if (uuid == null) {
                    throw new IllegalStateException("El servicio de almacenamiento no retornó un UUID válido.");
                }

                // Actualizar directamente en la tabla CREDENCIALES_SOCIOS
                String updateSql = "UPDATE CREDENCIALES_SOCIOS " +
                        "SET CRD_UUID_CREDENCIAL = ? " +
                        "WHERE CRD_MEM_MEMBRESIA = ? " +
                        "  AND CRD_NUMERO_CREDENCIAL_ID = ? " +
                        "  AND CRD_BEN_NUMBENEFICIARIO = ? " +
                        "  AND CRD_AÑO_VIGENCIA = ?";

                jdbcTemplate.update(updateSql, uuid, membresia, credencialId, beneficiario, anioVigencia);

                log.info("Migración de credencial exitosa para: Membresía={}, CredencialId='{}', UUID={}", membresia, credencialId, uuid);
                exitos++;
            } catch (Exception e) {
                log.error("Error al migrar credencial para Membresía: {}, CredencialId: {}. Detalle: {}", membresia, credencialId, e.getMessage(), e);
                fallos++;
            }
        }

        log.info("Migración de credenciales finalizada. Éxitos: {}, Fallos: {}", exitos, fallos);
    }

    // =====================================================
    // HELPERS de utilidad
    // =====================================================

    private byte[] decodeBase64(String base64Str) {
        if (base64Str == null) return null;
        base64Str = base64Str.trim();
        if (base64Str.startsWith("data:")) {
            int commaIdx = base64Str.indexOf(',');
            if (commaIdx != -1) {
                base64Str = base64Str.substring(commaIdx + 1);
            }
        }
        base64Str = base64Str.replaceAll("\\s+", "");
        return Base64.getDecoder().decode(base64Str);
    }

    private String determinarMimeType(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "image/jpeg";
        }
        String lower = nombreArchivo.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return "image/jpeg";
    }

    private String determinarMimeTypePorBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return "image/jpeg";
        }
        if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) {
            return "image/png";
        }
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49 && bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x38) {
            return "image/gif";
        }
        if (bytes[0] == (byte) 0x25 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x44 && bytes[3] == (byte) 0x46) {
            return "application/pdf";
        }
        return "image/jpeg";
    }

    private String determinarExtensionPorMimeType(String contentType) {
        if (contentType == null) return ".jpg";
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }
}
