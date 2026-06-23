package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.facil.modules.reservaciones.dto.request.ContactoDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.ExperienciaPortalDto;
import com.coralclubes.facil.modules.reservaciones.service.PortalService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/reservaciones/portal")
@RequiredArgsConstructor
public class PortalPublicController {

    private final PortalService service;

    @GetMapping("/experiencias")
    public ResponseEntity<ApiResponse<List<ExperienciaPortalDto>>> obtenerExperienciasPortal() {
        List<ExperienciaPortalDto> experiencias = service.obtenerExperienciasPortal();
        return ResponseEntity.ok(ApiResponse.success(experiencias));
    }

    @PostMapping("/contacto")
    public ResponseEntity<ApiResponse<Void>> enviarContacto(@Valid @RequestBody ContactoDto request) {
        service.enviarContacto(request);
        return ResponseEntity.ok(ApiResponse.success("Mensaje de contacto enviado exitosamente", null));
    }
}
