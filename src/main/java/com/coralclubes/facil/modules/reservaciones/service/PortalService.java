package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.projection.ExperienciaPortalProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.ContactoDto;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarExperienciaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarImagenPortalCompraMembresiaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.ExperienciaPortalDto;
import com.coralclubes.facil.modules.reservaciones.repository.PortalRepository;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalService {

    private final PortalRepository repo;
    private final StorageClient storageClient;
    private final NotificationClient notificationClient;
    private final ReservacionesRepository reservacionesRepo;

    private static final String STORAGE_FOLDER = "reservaciones/portal";
    private static final String KEY_IMG_BANNER_COMPRA_MEMBRESIA = "IMG_PORTAL_COMPRA_MEMBRESIA";
    private static final String KEY_TERMS_AND_CONDITIONS = "TERM_COND";
    private static final String KEY_PRIVACY_POLICY = "AVISO_PRIVACIDAD";

    @Value("${app.email.reservations}")
    private String emailReservations;

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
        String imagenAnterior = null;
        if (request.id() != null) {
            imagenAnterior = repo.spResvObtenerExperienciasPortal().stream()
                    .filter(e -> Objects.equals(e.id(), request.id()))
                    .map(ExperienciaPortalProjection::img)
                    .findFirst()
                    .orElse(null);
        }

        Integer idGuardado = repo.spResvGuardarExperienciasPortal(request, usuario)
                .orElseThrow(() -> new RuntimeException("No se pudo guardar la experiencia del portal"));

        // Solo si se confirma el guardado de la nueva y la imagen anterior cambió, eliminamos físicamente la anterior
        if (imagenAnterior != null && !imagenAnterior.trim().equalsIgnoreCase(request.img() != null ? request.img().trim() : "")) {
            eliminarArchivoFisicoSiEsUuid(imagenAnterior);
        }

        return idGuardado;
    }

    public void eliminarExperiencia(Integer id, String usuario) {
        String imagenAEliminar = repo.spResvObtenerExperienciasPortal().stream()
                .filter(e -> Objects.equals(e.id(), id))
                .map(ExperienciaPortalProjection::img)
                .findFirst()
                .orElse(null);

        repo.spResvEliminarExperienciasPortal(id, usuario);

        // Confirmada la eliminación en BD, procedemos con la eliminación física en storage
        eliminarArchivoFisicoSiEsUuid(imagenAEliminar);
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
                .destinatarios(List.of(emailReservations))
                .asunto("Nuevo contacto desde el portal - " + request.nombre())
                .codigoPlantilla("contacto-portal-v1")
                .variables(variables)
                .build();

        notificationClient.enviarNotificacion(notificacion);
    }

    public void enviarSolicitudInformacion(ContactoDto request) {
        log.info("Enviando solicitud de información desde el portal: {}", request);
    }

    public void guardarImagenPortalCompraMembresia(GuardarImagenPortalCompraMembresiaRequest request, String usuario) {
        String imagenAnterior = reservacionesRepo.fnResvObtenerValorParametroWeb(KEY_IMG_BANNER_COMPRA_MEMBRESIA).orElse(null);

        repo.spResvGuardarImagenPortalCompraMembresia(request.img(), usuario);

        // Solo si se confirma el guardado de la nueva y la imagen anterior cambió, eliminamos físicamente la anterior
        if (imagenAnterior != null && !imagenAnterior.trim().equalsIgnoreCase(request.img() != null ? request.img().trim() : "")) {
            eliminarArchivoFisicoSiEsUuid(imagenAnterior);
        }
    }

    public String obtenerImagenPortalCompraMembresia() {
        String valor = reservacionesRepo.fnResvObtenerValorParametroWeb(KEY_IMG_BANNER_COMPRA_MEMBRESIA).orElse(null);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(valor.trim());
            return storageClient.obtenerUrlDescarga(uuid).urlDescarga();
        } catch (IllegalArgumentException e) {
            return valor;
        }
    }

    /**
     * Elimina físicamente de forma permanente un archivo en Coral Storage si el valor corresponde a un UUID válido.
     */
    private void eliminarArchivoFisicoSiEsUuid(String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        try {
            UUID uuid = UUID.fromString(valor.trim());
            storageClient.eliminarArchivo(uuid, true);
            log.info("Archivo físico anterior eliminado exitosamente de Coral Storage: {}", uuid);
        } catch (IllegalArgumentException e) {
            log.debug("El valor no es un UUID, se omite eliminación en storage: {}", valor);
        } catch (Exception e) {
            log.error("Error al eliminar archivo físico anterior de Coral Storage ({}): {}", valor, e.getMessage(), e);
        }
    }
}
