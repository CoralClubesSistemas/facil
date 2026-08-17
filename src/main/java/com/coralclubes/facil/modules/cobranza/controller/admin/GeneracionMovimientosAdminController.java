package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.modules.cobranza.service.GeneracionMovimientosService;
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
@RequestMapping("/api/v1/admin/cobranza/generacion-movimientos")
@RequiredArgsConstructor
public class GeneracionMovimientosAdminController {

    private final GeneracionMovimientosService service;

    @GetMapping("/tipos-movimientos")
    @PreAuthorize("hasAuthority('MOD_SMNUGENERAMOVIMIENTOS')")
    public ResponseEntity<ApiResponse<List<MovimientoPorTipoMembresiaResponse>>> obtenerTiposMovimientos(
            @RequestParam Integer tipoMembresia
    ) {
        List<MovimientoPorTipoMembresiaResponse> movimientos = service.obtenerMovimientosPorTipoMembresia(tipoMembresia);
        return ResponseEntity.ok(ApiResponse.success(
                "Tipos de movimientos obtenidos exitosamente.",
                movimientos
        ));
    }
}
