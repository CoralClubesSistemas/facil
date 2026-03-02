package com.coralclubes.facil.modules.reservaciones.controller.privado;

import com.coralclubes.facil.modules.reservaciones.dto.request.ConsumoOfertaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.PromocionIntegralRequest;
import com.coralclubes.facil.modules.reservaciones.service.PromocionesService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promociones")
@RequiredArgsConstructor
public class PromocionesAdminController {

    private final PromocionesService service;

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarPromocion(
            @Valid @RequestBody PromocionIntegralRequest request) {
        return ResponseEntity.ok(service.guardarPromocion(request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONES')")
    public ResponseEntity<ApiResponse<Boolean>> eliminarPromocion(@PathVariable Integer id) {
        return ResponseEntity.ok(service.eliminarPromocion(id));
    }

    @GetMapping("/validar/{codigo}")
    public ResponseEntity<ApiResponse<Promocion>> validarCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.validarPromocionPorCodigo(codigo));
    }

    @PostMapping("/aplicar-consumo")
    public ResponseEntity<ApiResponse<Integer>> aplicarConsumo(
            @Valid @RequestBody ConsumoOfertaRequest request) {
        return ResponseEntity.ok(service.aplicarConsumoOferta(request));
    }
}