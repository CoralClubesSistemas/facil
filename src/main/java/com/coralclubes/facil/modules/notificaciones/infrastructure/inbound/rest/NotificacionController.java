package com.coralclubes.facil.modules.notificaciones.infrastructure.inbound.rest;

import com.coralclubes.facil.modules.notificaciones.application.dto.EnviarNotificacionMasivaRequest;
import com.coralclubes.facil.modules.notificaciones.application.dto.NotificacionDto;
import com.coralclubes.facil.modules.notificaciones.application.service.NotificacionEmisorService;
import com.coralclubes.facil.modules.notificaciones.application.service.NotificacionGestionService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {
    private final UserContext userContext;

    private final NotificacionGestionService gestionService;
    private final NotificacionEmisorService emisorService;

    @GetMapping("/no-leidas")
    public ResponseEntity<List<NotificacionDto>> obtenerNoLeidas() {
        List<NotificacionDto> notificaciones = gestionService.obtenerNoLeidas();
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/contador")
    public ResponseEntity<Long> contarNoLeidas() {
        long contador = gestionService.contarNoLeidas();
        return ResponseEntity.ok(contador);
    }

    @PutMapping("/{id}/marcar-leida")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        gestionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        gestionService.marcarTodasComoLeidas();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enviar")
    @PreAuthorize("hasAuthority('AUTH_NOTIFICACIONES')")
    public ApiResponse<Boolean> enviarNotificacionMasiva(
            @RequestBody EnviarNotificacionMasivaRequest request
    ) {
        String username = userContext.getUsername();
        emisorService.enviarAMultiples(username, request.destinatarios(), request.contenido());

        return ApiResponse.success("Notificaciones enviadas exitosamente.", true);
    }
}