package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.clientes.service.PuntosService;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosMembresia;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.facil.modules.cobranza.dto.request.EmailRequestDto;
import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.SintetizarCuerpoCorreoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuerpoCorreoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCuentaAdeudoDto;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.facil.modules.sistema.service.PlantillasCuerpoCorreoService;
import com.coralclubes.facil.modules.usuarios.service.UsuarioService;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final NotificationClient notificationClient;
    private final UsuarioService usuarioService;
    private final StorageClient storageClient;
    private final PlantillasCuerpoCorreoService plantillasService;
    private final SociosService sociosService;
    private final MovimientosClienteService movimientosClienteService;
    private final PuntosService puntosService;

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
                .adjuntos(request.adjuntosUrl())
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

    public CuerpoCorreoResponse sintetizarCuerpoCorreo(String membresia, SintetizarCuerpoCorreoRequest request) {
        Map<String, Object> variables = new HashMap<>();

        // 1. Consultar información del socio si es requerida por los campos
        if (request.nombre() || request.desarrollo() || request.membresia() || request.correo() || request.convenioCie()) {
            var apiResponseSocio = sociosService.obtenerSocios(membresia);
            var socio = apiResponseSocio != null ? apiResponseSocio.data() : null;
            if (socio != null) {
                if (request.nombre()) {
                    variables.put("socio", true);
                    variables.put("nombreSocio", socio.nombreCompleto());
                }
                if (request.desarrollo()) {
                    variables.put("desarrollo", socio.desarrollo());
                }
                if (request.membresia()) {
                    variables.put("membresia", socio.membresia());
                }
                if (request.correo()) {
                    variables.put("correoCliente", socio.correo());
                }
                if (request.convenioCie()) {
                    variables.put("convenioCie", sociosService.getBankNumberCie());
                    variables.put("referenciaCie", sociosService.calcularCIE(membresia));
                }
            }
        }

        // 2. Consultar adeudos y realizar sumas si se requieren adeudos o intereses
        if (request.totalAdeudo() || request.intereses()) {
            LocalDate finMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            LocalDateTime finMesTime = finMes.atTime(23, 59, 59);

            var requestAdeudo = EstadoCuentaAdeudoRequest.builder()
                    .membresia(membresia)
                    .fechaCorte(finMesTime)
                    .build();

            ApiResponse<List<EstadoCuentaAdeudoDto>> apiResponseAdeudo = movimientosClienteService.obtenerEstadoCuentaAdeudo(requestAdeudo, 0);
            List<EstadoCuentaAdeudoDto> list = (apiResponseAdeudo != null && apiResponseAdeudo.data() != null) ? apiResponseAdeudo.data() : List.of();

            BigDecimal sumInteres = BigDecimal.ZERO;
            BigDecimal sumNeto = BigDecimal.ZERO;

            for (var mov : list) {
                sumInteres = sumInteres.add(mov.interesMoratorio());
                sumNeto = sumNeto.add(mov.totalAPagar());
            }

            DecimalFormat moneyFormat = new DecimalFormat("$#,##0.00");

            if (request.totalAdeudo()) {
                variables.put("totalAdeudo", moneyFormat.format(sumNeto));
            }
            if (request.intereses() && sumInteres.compareTo(BigDecimal.ZERO) > 0) {
                variables.put("intereses", moneyFormat.format(sumInteres));
            }
        }

        // 3. Consultar saldo de puntos si es requerido
        if (request.saldoPuntos()) {
            try {
                PuntosMembresia puntos = puntosService.obtenerPuntosMembresia(membresia);
                if (puntos != null) {
                    variables.put("saldoPuntosDisponibles", puntos.saldoPuntosNeto());
                    variables.put("saldoPuntosUsados", puntos.puntosConsumidos());
                    variables.put("SaldoPuntosTotales", puntos.totalPuntosLiberados());

                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    variables.put("fechaReporte", java.time.LocalDateTime.now().format(formatter));
                }
            } catch (Exception e) {
                log.error("Error al obtener saldo de puntos para membresia: {}", membresia, e);
            }
        }

        // 4. Renderizar asunto y cuerpo
        String asunto = plantillasService.renderizarAsunto("CUERPO_POR_CAMPOS", variables);
        String cuerpo = plantillasService.renderizarCuerpo("CUERPO_POR_CAMPOS", variables);

        return CuerpoCorreoResponse.builder()
                .asunto(asunto)
                .cuerpo(cuerpo)
                .build();
    }
}
