package com.coralclubes.facil.shared.infrastructure.notificiones.infrastructure.inbound.rest;

import com.coralclubes.facil.shared.infrastructure.notificiones.application.dto.EnviarNotificacionMasivaRequest;
import com.coralclubes.facil.shared.infrastructure.notificiones.application.service.NotificacionEmisorService;
import com.coralclubes.facil.shared.infrastructure.notificiones.application.service.NotificacionGestionService;
import com.coralclubes.facil.shared.infrastructure.notificiones.domain.model.Notificacion;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionGestionService gestionService;
    private final NotificacionEmisorService emisorService;

    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> obtenerNoLeidas(Principal principal) {
        // principal.getName() extrae el username del JWT validado
        List<Notificacion> notificaciones = gestionService.obtenerNoLeidas(principal.getName());
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/contador")
    public ResponseEntity<Long> contarNoLeidas(Principal principal) {
        long contador = gestionService.contarNoLeidas(principal.getName());
        return ResponseEntity.ok(contador);
    }

    @PutMapping("/{id}/marcar-leida")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id, Principal principal) {
        gestionService.marcarComoLeida(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas(Principal principal) {
        gestionService.marcarTodasComoLeidas(principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enviar")
    public ApiResponse<Boolean> enviarNotificacionMasiva(
            @RequestBody EnviarNotificacionMasivaRequest request,
            Principal principal
    ) {
        // El remitente es el administrador autenticado que lanza la petición
        String remitente = principal.getName();

        emisorService.enviarAMultiples(remitente, request.destinatarios(), request.contenido());

        return ApiResponse.success("Notificaciones enviadas exitosamente.", true);
    }
}