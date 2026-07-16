package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
import com.coralclubes.facil.modules.clientes.service.BeneficiariosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/beneficiarios")
@RequiredArgsConstructor
public class BeneficiariosAdminController {

    private final BeneficiariosService service;

    @GetMapping("/{membresia}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BeneficiarioDto>>> obtenerBeneficiariosMembresia(
            @PathVariable String membresia
    ) {
        List<BeneficiarioDto> beneficiarios = service.obtenerBeneficiariosMembresia(membresia);
        return ResponseEntity.ok(ApiResponse.success("Beneficiarios obtenidos exitosamente.", beneficiarios));
    }

    @GetMapping("/{membresia}/pdf")
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
