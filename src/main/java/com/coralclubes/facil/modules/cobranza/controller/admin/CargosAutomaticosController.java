package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.response.RechazoCAResponse;
import com.coralclubes.facil.modules.cobranza.service.CargosAutomaticosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza/cargos-automaticos")
@RequiredArgsConstructor
public class CargosAutomaticosController {

    private final CargosAutomaticosService service;

    @GetMapping("/rechazos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<RechazoCAResponse>>> obtenerRechazosCA(
            @RequestParam String membresia
    ) {
        List<RechazoCAResponse> rechazos = service.obtenerRechazosCA(membresia);
        return ResponseEntity.ok(ApiResponse.success("Rechazos de cargos automáticos obtenidos exitosamente.", rechazos));
    }
}
