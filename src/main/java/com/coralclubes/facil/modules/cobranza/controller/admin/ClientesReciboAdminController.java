package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.application.usecases.ValidacionCancelacionReciboOrquestador;
import com.coralclubes.facil.modules.cobranza.service.RecibosService;
import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/portal/recibos/clientes")
@RequiredArgsConstructor
public class ClientesReciboAdminController {
    private final RecibosService recibosService;

    @GetMapping("/digital")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ArchivoDescarga>> obtenerReciboDigital(
            @RequestParam String membresia,
            @RequestParam Integer numeroRecibo,
            @RequestParam Integer idSerieRecibo
    ) {
        return ResponseEntity.ok(recibosService.obtenerUrlDescargaReciboDigital(membresia, numeroRecibo, idSerieRecibo));
    }
}
