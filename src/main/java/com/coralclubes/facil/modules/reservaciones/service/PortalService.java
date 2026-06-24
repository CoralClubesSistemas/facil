package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.projection.ExperienciaPortalProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.ContactoDto;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarExperienciaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.ExperienciaPortalDto;
import com.coralclubes.facil.modules.reservaciones.repository.PortalRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortalService {

    private final PortalRepository repo;
    private final StorageClient storageClient;
    private final NotificationClient notificationClient;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasConfiguracion;

    @Value("${app.clients.notifications.aliases.default}")
    private String aliasNotificaciones;

    private static final String STORAGE_FOLDER = "reservaciones/portal";

    public List<ExperienciaPortalDto> obtenerExperienciasPortal() {
        List<ExperienciaPortalProjection> projs = repo.spResvObtenerExperienciasPortal();
        return projs.stream().map(p -> {
            String urlResuelta = null;
            if (p.img() != null && !p.img().isBlank()) {
                try {
                    UUID uuid = UUID.fromString(p.img().trim());
                    urlResuelta = storageClient.obtenerUrlDescarga(uuid).urlDescarga();
                } catch (IllegalArgumentException e) {
                    urlResuelta = p.img(); // Si no es un UUID, se devuelve la cadena original
                }
            }
            return ExperienciaPortalDto.builder()
                    .id(p.id())
                    .tag(p.tag())
                    .titulo(p.titulo())
                    .descripcion(p.descripcion())
                    .link(p.link())
                    .img(urlResuelta)
                    .build();
        }).toList();
    }

    public Integer guardarExperiencia(GuardarExperienciaRequest request, String usuario) {
        return repo.spResvGuardarExperienciasPortal(request, usuario)
                .orElseThrow(() -> new RuntimeException("No se pudo guardar la experiencia del portal"));
    }

    public void eliminarExperiencia(Integer id, String usuario) {
        repo.spResvEliminarExperienciasPortal(id, usuario);
    }

    public RespuestaCargaDto solicitarUrlCarga(SolicitarUrlRequest request, String usuario) {
        Map<String, String> metadata = Map.of(
                "modulo", "PORTAL RESERVACIONES",
                "experienciaId", String.valueOf(request.id() != null ? request.id() : "NUEVO"),
                "subidoPor", usuario
        );

        SolicitudCargaDto solicitud = SolicitudCargaDto.builder()
                .nombreArchivo(request.nombreArchivo())
                .contentType(request.contentType())
                .tamanoBytes(request.tamanoBytes())
                .aliasConfiguracion(aliasConfiguracion)
                .esPublico(true)
                .rutaLogica(STORAGE_FOLDER)
                .metadatos(metadata)
                .build();

        return storageClient.solicitarUrlCarga(solicitud);
    }

    public void enviarContacto(ContactoDto request) {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("nombre", request.nombre());
        variables.put("email", request.email());
        variables.put("telefono", request.telefono());
        variables.put("hotelInteres", request.hotelInteres() != null ? request.hotelInteres() : "No especificado");
        variables.put("mensaje", request.mensaje());

        SolicitudNotificacionDto notificacion = SolicitudNotificacionDto.builder()
                .aliasConfig(aliasNotificaciones)
                .destinatarios(List.of("lvivar@coralclubes.com"))
                .asunto("Nuevo contacto desde el portal - " + request.nombre())
                .codigoPlantilla("contacto-portal-v1")
                .variables(variables)
                .build();

        notificationClient.enviarNotificacion(notificacion);
    }
}
