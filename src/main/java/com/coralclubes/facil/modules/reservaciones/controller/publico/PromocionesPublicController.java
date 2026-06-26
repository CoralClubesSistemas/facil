package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.facil.modules.reservaciones.dto.request.ConsumoOfertaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.PromocionPortalDto;
import com.coralclubes.facil.modules.reservaciones.service.PromocionesService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el consumo de Promociones desde Portales Web y Apps Móviles.
 * Maneja tanto la consulta anónima (validación) como el consumo protegido (socios).
 */
@RestController
@RequestMapping("/api/v1/public/reservaciones/promociones")
@RequiredArgsConstructor
public class PromocionesPublicController {

    private final PromocionesService service;

    @GetMapping("/portal")
    public ResponseEntity<ApiResponse<List<PromocionPortalDto>>> obtenerPromocionesPortal(
            @RequestParam(required = false) String membresia
    ) {
        return ResponseEntity.ok(service.obtenerPromocionesPortal(membresia));
    }
}