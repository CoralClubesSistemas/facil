package com.coralclubes.facil.modules.clientes.migration;

import com.coralclubes.facil.modules.clientes.repository.NotasClientesRepository;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaLegacyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aplicación y ejecutora independiente para migrar imágenes en Base64
 * desde la tabla legacy IMAGENES_NOTAS_CLIENTES hacia Coral Storage y registrarlas en ADJUNTOS_NOTAS_CLIENTES.
 */
@SpringBootApplication(scanBasePackages = "com.coralclubes.facil")
@Profile("migration")
@Component
@Slf4j
public class NotasClientesMigrationApp implements CommandLineRunner {

    /**
     * Define un RestClient alternativo y primario para cuando el perfil 'migration' esté activo.
     * Esto permite asignar un ReadTimeout y ConnectTimeout prolongados (5 minutos) para evitar excepciones
     * de timeout durante la subida síncrona de archivos pesados.
     */
    @Bean
    @Primary
    public static RestClient migrationRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(300000);   // 5 minutos en milisegundos
        requestFactory.setConnectTimeout(30000);  // 30 segundos en milisegundos

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NotasClientesMigrationApp.class);
        // Activar el perfil "migration" de manera programática para que se registre este Bean al arrancar
        app.setAdditionalProfiles("migration");
        // Desactivar el servidor web embebido para que se ejecute solo como comando de consola
        System.setProperty("spring.main.web-application-type", "none");
        app.run(args);
    }

    private final JdbcTemplate jdbcTemplate;
    private final StorageClient storageClient;
    private final NotasClientesRepository repository;
    private final String aliasStorageDefault;

    public NotasClientesMigrationApp(
            JdbcTemplate jdbcTemplate,
            StorageClient storageClient,
            NotasClientesRepository repository,
            @Value("${app.clients.storage.aliases.default}") String aliasStorageDefault) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageClient = storageClient;
        this.repository = repository;
        this.aliasStorageDefault = aliasStorageDefault;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== INICIANDO PROCESO DE MIGRACIÓN DE IMÁGENES A CORAL STORAGE ===");

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

        log.info("Registros elegibles encontrados para migración: {}", records.size());

        int exitos = 0;
        int fallos = 0;

        for (Map<String, Object> record : records) {
            String membresia = (String) record.get("membresia");
            Integer consecutivo = (Integer) record.get("consecutivo");
            String nombreArchivo = (String) record.get("nombreArchivo");
            String contenidoBase64 = (String) record.get("contenidoBase64");
            String usuario = (String) record.get("usuario");
            Integer orden = (Integer) record.get("orden");

            log.info("Procesando registro: Membresía={}, Consecutivo={}, Archivo='{}', Orden={}", 
                    membresia, consecutivo, nombreArchivo, orden);

            try {
                // Decodificar Base64
                byte[] fileBytes = decodeBase64(contenidoBase64);
                if (fileBytes == null || fileBytes.length == 0) {
                    log.warn("Contenido decodificado vacío para Membresía {}, Consecutivo {}. Saltando.", membresia, consecutivo);
                    fallos++;
                    continue;
                }

                // Determinar tipo MIME
                String contentType = determinarMimeType(nombreArchivo);

                // Ruta lógica para almacenar (idéntico al flujo estándar de NotasClientesService)
                String rutaLogica = "notas/archivos-socios/" + membresia + "/" + consecutivo;

                // Crear petición legacy para upload síncrono
                SolicitudCargaLegacyDto solicitud = SolicitudCargaLegacyDto.builder()
                        .idCorrelacion(membresia + "-" + consecutivo + "-" + orden)
                        .aliasConfiguracion(aliasStorageDefault)
                        .metadatos(Map.of(
                                "modulo", "CLIENTES",
                                "membresia", membresia,
                                "notaConsecutivo", String.valueOf(consecutivo),
                                "subidoPor", usuario != null ? usuario : "MIGRACION",
                                "fechaMigracion", String.valueOf(System.currentTimeMillis())
                        ))
                        .esPublico(false)
                        .rutaLogica(rutaLogica)
                        .requiereDepuracion(true)
                        .build();

                // Cargar síncronamente a Coral Storage
                InfoArchivoDto response = storageClient.cargarArchivoSincrono(fileBytes, nombreArchivo, contentType, solicitud);
                UUID uuid = response.uuid();

                if (uuid == null) {
                    throw new IllegalStateException("El servicio de almacenamiento no retornó un UUID válido.");
                }

                // Registrar en la nueva tabla usando el Stored Procedure existente
                repository.spRegistrarArhivosNotas(
                        membresia,
                        consecutivo,
                        nombreArchivo,
                        uuid.toString(),
                        contentType,
                        usuario != null ? usuario : "MIGRACION"
                );

                log.info("Migración exitosa para: Membresía={}, Consecutivo={}, Archivo={}. UUID asignado: {}", 
                        membresia, consecutivo, nombreArchivo, uuid);
                exitos++;

            } catch (Exception e) {
                log.error("Error al migrar archivo para Membresía: {}, Consecutivo: {}, Archivo: {}. Detalle: {}",
                        membresia, consecutivo, nombreArchivo, e.getMessage(), e);
                fallos++;
            }
        }

        log.info("=== MIGRACIÓN FINALIZADA ===");
        log.info("Total procesados: {}", records.size());
        log.info("Éxitos: {}", exitos);
        log.info("Fallos: {}", fallos);
    }

    private byte[] decodeBase64(String base64Str) {
        if (base64Str == null) return null;
        base64Str = base64Str.trim();
        // Limpiar prefijo data URI si existe (ej. data:image/png;base64,)
        if (base64Str.startsWith("data:")) {
            int commaIdx = base64Str.indexOf(',');
            if (commaIdx != -1) {
                base64Str = base64Str.substring(commaIdx + 1);
            }
        }
        // Remover espacios o saltos de línea que puedan corromper la decodificación
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
}
