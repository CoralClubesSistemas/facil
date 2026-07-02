package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.EmailRequestDto;
import com.coralclubes.facil.modules.usuarios.service.UsuarioService;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final NotificationClient notificationClient;
    private final UsuarioService usuarioService;
    private final StorageClient storageClient;

    @Value("${app.clients.notifications.aliases.default}")
    private String aliasConfig;

    public void enviarCorreo(EmailRequestDto request, String username) {
        // 1. Obtener datos de correo del usuario y validar
        var datosCorreo = usuarioService.obtenerDatosCorreoUsuario(username)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron los datos de correo para el usuario: " + username));

        if (datosCorreo.correoAutorizado() == null || datosCorreo.correoAutorizado().isBlank()) {
            throw new IllegalArgumentException("El usuario no tiene correo autorizado");
        }

        // 2. Obtener URL de descarga y descargar archivo de firma a una variable byte[]
        byte[] firmaBytes = null;
        if (datosCorreo.imagenFirma() != null && !datosCorreo.imagenFirma().isBlank()) {
            try {
                UUID uuidFirma = UUID.fromString(datosCorreo.imagenFirma());
                var archivoDescarga = storageClient.obtenerUrlDescarga(uuidFirma);
                if (archivoDescarga != null && archivoDescarga.urlDescarga() != null) {
                    URI uri = URI.create(archivoDescarga.urlDescarga());
                    firmaBytes = RestClient.create().get()
                            .uri(uri)
                            .retrieve()
                            .body(byte[].class);
                }
            } catch (Exception e) {
                log.error("Error al descargar la imagen de firma del usuario {}: {}", username, e.getMessage());
            }
        }

        // 3. Preparar variables para la plantilla
        Map<String, Object> variables = new HashMap<>();
        variables.put("cuerpoCorreo", request.cuerpo());
        variables.put("cidImagenEmbebida", request.nombreImagen());
        variables.put("linkImagenEmbebida", request.linkImagenEmbebida());
        variables.put("cidFirma", datosCorreo.imagenFirma());
        variables.put("whatsappContacto", datosCorreo.telefono());

        // 4. Construir solicitud de notificación mandando contrasenaCorreo en el campo password
        SolicitudNotificacionDto solicitud = SolicitudNotificacionDto.builder()
                .aliasConfig(aliasConfig)
                .destinatarios(request.destinatarios())
                .asunto(request.asunto())
                .codigoPlantilla("email-corporativo-v1")
                .variables(variables)
                .remitenteOverride(datosCorreo.correoAutorizado())
                .password(datosCorreo.contrasenaCorreo())
                .build();

        Map<String, byte[]> archivos = new HashMap<>();
        // Agregar imagen embebida si existe
        if (request.nombreImagen() != null && request.contenidoImagen() != null) {
            archivos.put(request.nombreImagen(), request.contenidoImagen());
        }

        // Agregar la firma del usuario como adjunto si existe
        if (firmaBytes != null && datosCorreo.imagenFirma() != null) {
            archivos.put(datosCorreo.imagenFirma(), firmaBytes);
        }

        // Agregar archivos adjuntos adicionales si existen (no se mapean como variables)
        if (request.adjuntos() != null) {
            for (var adjunto : request.adjuntos()) {
                if (adjunto.nombre() != null && adjunto.contenido() != null) {
                    archivos.put(adjunto.nombre(), adjunto.contenido());
                }
            }
        }

        notificationClient.enviarNotificacionConAdjuntos(solicitud, archivos);
    }
}
