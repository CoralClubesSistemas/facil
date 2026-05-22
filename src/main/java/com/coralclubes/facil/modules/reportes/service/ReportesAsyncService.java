package com.coralclubes.facil.modules.reportes.service;

import com.coralclubes.facil.modules.notificaciones.application.dto.PeticionNotificacionDto;
import com.coralclubes.facil.modules.notificaciones.application.service.NotificacionEmisorService;
import com.coralclubes.facil.modules.reportes.dto.request.EjecutarReporteRequest;
import com.coralclubes.facil.modules.reportes.repository.ReportesMotorRepository;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.utils.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportesAsyncService {

    private final ReportesMotorService motorService;
    private final ExcelExportService excelExportService;
    private final ReportesMotorRepository repository;

    // Integraciones
    private final StorageClient storageClient;
    private final NotificacionEmisorService notificacionService;

    // Constantes de configuración
    private static final String MIME_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorage;

    /**
     * Procesa la generación del reporte, genera el Excel, lo sube al microservicio de almacenamiento,
     * actualiza la bitácora y envía una notificación en tiempo real al usuario.
     */
    @Async
    public void procesarReporteYSubirAsincrono(EjecutarReporteRequest request, String nombreReporteBase, Integer idBitacora, String usuario) {
        log.info("Iniciando procesamiento asíncrono. Reporte: {}, Bitácora ID: {}, Usuario: {}", nombreReporteBase, idBitacora, usuario);

        String nombreArchivoFinal = excelExportService.generarNombreArchivo(nombreReporteBase);

        try {
            // 1. Ejecutar el reporte en BD (También persiste favoritos y columnas seleccionadas)
            List<Map<String, Object>> datosCrudos = motorService.ejecutarYpersistir(request);

            // 2. Convertir los datos a un arreglo de bytes (Formato Excel)
            byte[] excelBytes = excelExportService.generarExcelBytes(datosCrudos, nombreReporteBase);

            // 3. Negociar la URL de Carga (Patrón Valet Key con Coral Storage)
            String rutaLogica = "reportes/" + Year.now().getValue() + "/" + usuario;

            SolicitudCargaDto solicitudStorage = SolicitudCargaDto.builder()
                    .requiereDepuracion(false)
                    .nombreArchivo(nombreArchivoFinal)
                    .contentType(MIME_TYPE_EXCEL)
                    .tamanoBytes((long) excelBytes.length)
                    .esPublico(false) // Los reportes son estrictamente privados
                    .aliasConfiguracion(aliasStorage)
                    .rutaLogica(rutaLogica)
                    .metadatos(Map.of(
                            "idBitacora", String.valueOf(idBitacora),
                            "usuarioGenerador", usuario,
                            "modulo", "REPORTES"
                    ))
                    .build();

            RespuestaCargaDto respuestaCarga = storageClient.solicitarUrlCarga(solicitudStorage);
            UUID fileUuid = respuestaCarga.fileId();

            // 4. Subir el binario directamente al Storage (MinIO/S3) usando la URL pre-firmada
            storageClient.subirArchivoBinario(respuestaCarga.uploadUrl(), excelBytes, MIME_TYPE_EXCEL);
            log.info("Reporte subido exitosamente a Coral Storage. FileId: {}", fileUuid);

            // 5. Marcar la bitácora como completada en la Base de Datos
            repository.actualizarFinReporte(idBitacora, "COMPLETADO", fileUuid, null);

            // 6. Enviar Notificación WebSocket al usuario
            enviarNotificacionWebSocket(usuario,
                    "Reporte Completado",
                    "Tu reporte '" + nombreReporteBase + "' se ha generado exitosamente. Ya puedes descargarlo.",
                    "SUCCESS",
                    1,
                    idBitacora,
                    fileUuid,
                    request.modulo().toString()
            );

        } catch (Exception e) {
            log.error("Fallo crítico en procesamiento asíncrono (Bitácora ID: {}): {}", idBitacora, e.getMessage(), e);

            // Marcar bitácora con error (Truncamos el mensaje para evitar desbordar el VARCHAR de SQL Server)
            String mensajeError = e.getMessage();
            if (mensajeError != null && mensajeError.length() > 490) {
                mensajeError = mensajeError.substring(0, 490);
            }
            repository.actualizarFinReporte(idBitacora, "ERROR", null, mensajeError);

            // Notificar al usuario del fallo
            enviarNotificacionWebSocket(usuario,
                    "Error al generar reporte",
                    "Ocurrió un problema al generar tu reporte '" + nombreReporteBase + "'. " + mensajeError,
                    "ERROR",
                    3,
                    idBitacora,
                    null,
                    request.modulo().toString()
            );
        }
    }

    /**
     * Utilería privada para estructurar y enviar la notificación
     */
    private void enviarNotificacionWebSocket(String destinatario, String titulo, String mensaje, String tipo, int prioridad, Integer idBitacora, UUID fileUuid, String modulo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("modulo", "REPORTES");
        metadata.put("idBitacora", idBitacora);

        // Si el archivo se generó con éxito, mandamos el UUID y una acción sugerida para el Frontend
        if (fileUuid != null) {
            metadata.put("fileId", fileUuid.toString());
            metadata.put("accion", "DESCARGAR_REPORTE");
            metadata.put("urlDestino", "/app/reportes?reporte=" + modulo);
        }

        PeticionNotificacionDto alerta = new PeticionNotificacionDto(
                tipo,
                prioridad,
                titulo,
                mensaje,
                metadata
        );

        // Remitente 'SISTEMA' como estándar para notificaciones automáticas del servidor
        notificacionService.enviarAUsuario("SISTEMA", destinatario, alerta);
    }
}