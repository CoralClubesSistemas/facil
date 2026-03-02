package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.facil.modules.reservaciones.dto.request.ConsumoOfertaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion;
import com.coralclubes.facil.modules.reservaciones.service.PromocionesService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el consumo de Promociones desde Portales Web y Apps Móviles.
 * Maneja tanto la consulta anónima (validación) como el consumo protegido (socios).
 */
@RestController
@RequestMapping("/api/v1/public/reservaciones/promociones")
@RequiredArgsConstructor
public class PromocionesPublicController {

    private final PromocionesService service;

    /**
     * Endpoint abierto. Permite que un cliente valide un código en su carrito
     * para ver si existe y qué beneficios da, incluso antes de hacer login.
     */
    @GetMapping("/validar/{codigo}")
    public ResponseEntity<ApiResponse<Promocion>> validarCodigo(@PathVariable String codigo) {
        ApiResponse<Promocion> response = service.validarPromocionPorCodigo(codigo);
        return ResponseEntity.status(response.status()).body(response);
    }

    /**
     * Endpoint protegido. Aplica formalmente la promoción, reduciendo el stock.
     */
    @PostMapping("/aplicar-consumo")
    public ResponseEntity<ApiResponse<Integer>> aplicarConsumo(
            @Valid @RequestBody ConsumoOfertaRequest request) {
        ApiResponse<Integer> response = service.aplicarConsumoOferta(request);
        return ResponseEntity.status(response.status()).body(response);
    }
}