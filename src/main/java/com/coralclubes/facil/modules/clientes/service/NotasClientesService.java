package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.clientes.dto.request.CrearNotaUsuarioRequest;
import com.coralclubes.facil.modules.clientes.dto.response.CrearNotaUsuarioResponse;
import com.coralclubes.facil.modules.clientes.dto.response.NotasClienteResponse;
import com.coralclubes.facil.modules.clientes.dto.response.ObtenerArchivosNotaResponse;
import com.coralclubes.facil.modules.clientes.repository.NotasClientesRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotasClientesService {
    private final NotasClientesRepository repository;
    private final UserContext userContext;
    private final BusinessLogger businessLogger;
    private final StorageClient storageClient;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorageDefault;

    public ApiResponse<List<NotasClienteResponse>> buscarNotasCliente(
            String numeroMembresia,
            LocalDateTime fechaRangoInicial,
            LocalDateTime fechaRangoFinal,
            Integer clasificaNota
    ) {
        return ApiResponse.success(
                "Notas de cliente obtenidas exitosamente.",
                repository.spBuscarNotasCliente(numeroMembresia, fechaRangoInicial, fechaRangoFinal, clasificaNota)
        );
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerClasificacionesXUsuario() {
        Integer rolId = userContext.getRoleId();

        return ApiResponse.success(repository.spObtenerClasificacionNotasXUsuario(rolId));
    }

    /**
     * Crea una nota para un cliente (membresía).
     * Orquesta la llamada al SP, auditoría y respuesta.
     */
    public ApiResponse<CrearNotaUsuarioResponse> crearNota(CrearNotaUsuarioRequest request) {
        // Extraer identidad del usuario desde el contexto de seguridad
        String usuarioActual = userContext.getUsername();

        // Ejecutar el SP para crear la nota
        CrearNotaUsuarioResponse response = repository.spCrearNotaUsuario(
                request.membresia(),
                usuarioActual,
                request.clasificacionNota(),
                request.nota(),
                request.alerta()
        ).orElseThrow(() -> new RuntimeException("Error al crear la nota para la membresía: " + request.membresia()));

        // Registrar auditoría de negocio
        businessLogger.info(
                usuarioActual,
                "Nota creada para membresía: {} | Clasificación: {} | Alerta: {}",
                request.membresia(),
                request.clasificacionNota(),
                request.alerta()
        );

        return ApiResponse.success("Nota creada exitosamente", response);
    }

    /**
     * Solicita URLs de carga prefirmadas para adjuntos de una nota.
     * Construye rutas lógicas y metadatos asociados a la membresía y consecutivo.
     *
     * @param membresia    Identificador de membresía
     * @param consecutivo  Número consecutivo de la nota
     * @param solicitudes  Lista de archivos a cargar (nombre, tipo, tamaño)
     * @return URLs de carga prefirmadas del servicio de almacenamiento
     */
    public ApiResponse<List<RespuestaCargaDto>> solicitarUrlsDeCargaArchivos(
            String membresia,
            Integer consecutivo,
            List<SolicitarUrlRequest> solicitudes
    ) {
        String usuario = userContext.getUsername();

        // Construir ruta lógica inmutable para archivos de notas
        String rutaLogica = "notas/archivos-socios/" + membresia + "/" + consecutivo + "/";

        List<RespuestaCargaDto> respuestas = solicitudes.stream()
                .map(solicitud -> {
                    String ruta = rutaLogica + solicitud.id();

                    SolicitudCargaDto solicitudStorage = SolicitudCargaDto.builder()
                            .nombreArchivo(solicitud.nombreArchivo())
                            .contentType(solicitud.contentType())
                            .tamanoBytes(solicitud.tamanoBytes())
                            .aliasConfiguracion(aliasStorageDefault)
                            .esPublico(false)
                            .rutaLogica(ruta)
                            .metadatos(Map.of(
                                    "modulo", "CLIENTES",
                                    "membresia", membresia,
                                    "notaConsecutivo", String.valueOf(consecutivo),
                                    "subidoPor", usuario
                            ))
                            .build();

                    return storageClient.solicitarUrlCarga(solicitudStorage);
                })
                .toList();

        businessLogger.info(usuario,
                "URLs de carga solicitadas para nota - Membresía: {}, Consecutivo: {}, Cantidad: {}",
                membresia, consecutivo, solicitudes.size());

        return ApiResponse.success("URLs de carga solicitadas exitosamente.", respuestas);
    }

    /**
     * Registra archivos adjuntos en una nota existente.
     * Persiste la información de los archivos en el storage y base de datos.
     *
     * @param membresia      Identificador de membresía
     * @param consecutivo    Número consecutivo de la nota
     * @param nombreArchivo  Nombre del archivo
     * @param uuidArchivo    UUID único del archivo en storage
     * @param tipoArchivo    Tipo MIME del archivo
     */
    public void registrarArchivoNota(
            String membresia,
            Integer consecutivo,
            String nombreArchivo,
            String uuidArchivo,
            String tipoArchivo
    ) {
        String usuario = userContext.getUsername();

        try {
            java.util.UUID uuid = java.util.UUID.fromString(uuidArchivo);

            repository.spRegistrarArhivosNotas(
                    membresia,
                    consecutivo,
                    nombreArchivo,
                    uuid,
                    tipoArchivo,
                    usuario
            );

            businessLogger.info(usuario,
                    "Archivo registrado en nota - Membresía: {}, Consecutivo: {}, Archivo: {}",
                    membresia, consecutivo, nombreArchivo);

        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("UUID de archivo inválido: " + uuidArchivo);
        }
    }

    /**
     * Obtiene los archivos adjuntos de una nota específica con URLs de descarga.
     * Replica el patrón de HotelesService para enriquecimiento de recursos.
     *
     * @param membresia    Identificador de membresía
     * @param consecutivo  Número consecutivo de la nota
     * @return Lista de archivos asociados a la nota con URLs de descarga
     */
    public ApiResponse<List<ObtenerArchivosNotaResponse>> obtenerArchivosNota(String membresia, Integer consecutivo) {
        var archivos = repository.spObtenerArchivosNotas(membresia, consecutivo);

        if (archivos.isEmpty()) {
            return ApiResponse.success("No se encontraron archivos para esta nota.", List.of());
        }

        List<ObtenerArchivosNotaResponse> respuesta = archivos.stream()
                .map(archivo -> {
                    String urlDescarga = archivo.uuidArchivo() != null
                            ? storageClient.obtenerUrlDescarga(archivo.uuidArchivo())
                            : null;

                    return new ObtenerArchivosNotaResponse(
                            archivo.nombreArchivo(),
                            archivo.uuidArchivo(),
                            archivo.tipoArchivo(),
                            urlDescarga,
                            archivo.usuarioCarga(),
                            archivo.fechaCarga()
                    );
                })
                .toList();

        return ApiResponse.success("Archivos de nota obtenidos correctamente.", respuesta);
    }
}
