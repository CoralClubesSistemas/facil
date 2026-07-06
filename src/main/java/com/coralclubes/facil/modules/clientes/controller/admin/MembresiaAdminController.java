package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.MembresiaDatosDto;
import com.coralclubes.facil.modules.clientes.service.MembresiaService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/clientes/membresias")
@RequiredArgsConstructor
public class MembresiaAdminController {

    private final MembresiaService service;

    @GetMapping("/{membresia}/datos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MembresiaDatosDto>> obtenerDatosMembresia(
            @PathVariable String membresia,
            @RequestParam(required = false) Integer plan
    ) {
        MembresiaDatosDto datos = service.obtenerDatosMembresia(membresia, plan)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información para la membresía: " + membresia));

        return ResponseEntity.ok(ApiResponse.success("Datos de membresía obtenidos exitosamente.", datos));
    }
}
