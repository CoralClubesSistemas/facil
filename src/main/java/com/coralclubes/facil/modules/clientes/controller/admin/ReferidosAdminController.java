package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiosReferidosResponse;
import com.coralclubes.facil.modules.clientes.dto.response.DetalleConsumoReferidoResponse;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaReferidoDto;
import com.coralclubes.facil.modules.clientes.dto.response.ResumenReferidosResponse;
import com.coralclubes.facil.modules.clientes.service.ReferidosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/referidos")
@RequiredArgsConstructor
public class ReferidosAdminController {

    private final ReferidosService service;

    @GetMapping("/{membresia}/beneficios")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BeneficiosReferidosResponse>>> obtenerBeneficiosReferidos(
            @PathVariable String membresia
    ) {
        List<BeneficiosReferidosResponse> beneficios = service.obtenerBeneficiosReferidos(membresia);
        return ResponseEntity.ok(ApiResponse.success("Beneficios de referidos obtenidos exitosamente.", beneficios));
    }

    @GetMapping("/{membresia}/detalle-consumo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DetalleConsumoReferidoResponse>>> obtenerDetalleConsumoReferido(
            @PathVariable String membresia,
            @RequestParam Integer consecutivo
    ) {
        List<DetalleConsumoReferidoResponse> detalle = service.obtenerDetalleConsumoReferido(membresia, consecutivo);
        return ResponseEntity.ok(ApiResponse.success("Detalle de consumo de referido obtenido exitosamente.", detalle));
    }

    @GetMapping("/{membresia}/resumen")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResumenReferidosResponse>> obtenerResumenReferidos(
            @PathVariable String membresia
    ) {
        ResumenReferidosResponse resumen = service.obtenerResumenReferidos(membresia).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Resumen de referidos obtenido exitosamente.", resumen));
    }

    @GetMapping("/{membresia}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MembresiaReferidoDto>>> obtenerReferidos(
            @PathVariable String membresia
    ) {
        List<MembresiaReferidoDto> referidos = service.obtenerReferidos(membresia);
        return ResponseEntity.ok(ApiResponse.success("Referidos de la membresía obtenidos exitosamente.", referidos));
    }
}
