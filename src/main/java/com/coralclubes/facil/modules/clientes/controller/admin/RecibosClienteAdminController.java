package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.ReciboClienteDto;
import com.coralclubes.facil.modules.clientes.service.RecibosClienteService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/recibos")
@RequiredArgsConstructor
public class RecibosClienteAdminController {

    private final RecibosClienteService service;

    @GetMapping("/{membresia}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReciboClienteDto>>> obtenerRecibosMembresia(
            @PathVariable String membresia
    ) {
        List<ReciboClienteDto> recibos = service.obtenerRecibosMembresia(membresia);
        return ResponseEntity.ok(ApiResponse.success("Recibos de membresía obtenidos exitosamente.", recibos));
    }
}
