package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.request.BloqueoBeneficiarioRequest;
import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
import com.coralclubes.facil.modules.clientes.service.BeneficiariosService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/beneficiarios")
@RequiredArgsConstructor
public class BeneficiariosAdminController {

    private final BeneficiariosService service;
    private final UserContext userContext;

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

    @PostMapping("/{membresia}/{numBeneficiario}/bloquear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> bloquearBeneficiario(
            @PathVariable String membresia,
            @PathVariable Integer numBeneficiario,
            @Valid @RequestBody BloqueoBeneficiarioRequest request
    ) {
        String usuario = userContext.getUsername();
        service.bloquearBeneficiario(membresia, numBeneficiario, request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Beneficiario bloqueado exitosamente.", null));
    }

    @PostMapping("/{membresia}/{numBeneficiario}/desbloquear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> desbloquearBeneficiario(
            @PathVariable String membresia,
            @PathVariable Integer numBeneficiario
    ) {
        String usuario = userContext.getUsername();
        service.desbloquearBeneficiario(membresia, numBeneficiario, usuario);
        return ResponseEntity.ok(ApiResponse.success("Beneficiario desbloqueado exitosamente.", null));
    }
}
