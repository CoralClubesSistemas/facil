package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaDatosDto;
import com.coralclubes.facil.modules.clientes.service.MembresiaService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{membresia}/beneficiarios")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BeneficiarioDto>>> obtenerBeneficiariosMembresia(
            @PathVariable String membresia
    ) {
        List<BeneficiarioDto> beneficiarios = service.obtenerBeneficiariosMembresia(membresia);
        return ResponseEntity.ok(ApiResponse.success("Beneficiarios obtenidos exitosamente.", beneficiarios));
    }

    @GetMapping("/{membresia}/beneficiarios-pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> obtenerBeneficiariosPdf(
            @PathVariable String membresia
    ) {
        byte[] pdf = service.generarPdfReporteBeneficiarios(membresia);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-beneficiarios-" + membresia + ".pdf")
                .body(pdf);
    }
}
