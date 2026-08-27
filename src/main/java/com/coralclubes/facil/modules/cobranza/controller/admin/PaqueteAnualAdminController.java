package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPaqueteAnualResponse;
import com.coralclubes.facil.modules.cobranza.service.PaqueteAnualService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza/paquete-anual")
@RequiredArgsConstructor
public class PaqueteAnualAdminController {

    private final PaqueteAnualService service;
    private final UserContext userContext;

    @GetMapping("/movimientos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<MovimientoPaqueteAnualResponse>>> obtenerMovimientosPaqueteAnual(
            @RequestParam Integer anio,
            @RequestParam Integer tipoMembresia
    ) {
        List<MovimientoPaqueteAnualResponse> movimientos = service.obtenerMovimientosPaqueteAnual(anio, tipoMembresia);
        return ResponseEntity.ok(ApiResponse.success("Movimientos de paquete anual obtenidos exitosamente.", movimientos));
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Integer>> guardarPaqueteAnual(
            @Valid @RequestBody GuardarPaqueteAnualRequest request
    ) {
        String usuario = userContext.getUsername();
        Integer idPaquete = service.guardarPaqueteAnual(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Paquete anual guardado exitosamente.", idPaquete));
    }
}
