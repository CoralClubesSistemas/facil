package com.coralclubes.facil.shared.platform.qr.controller;

import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResolucionResponse;
import com.coralclubes.facil.shared.platform.qr.service.SystemQrService;
import com.coralclubes.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST público para la consulta y resolución de códigos QR en el sistema.
 */
@RestController
@RequestMapping("/api/v1/public/sistema/qrs")
@RequiredArgsConstructor
@Tag(name = "Códigos QR del Sistema", description = "Endpoints públicos para consulta y resolución de códigos QR")
public class SystemQrController {

    private final SystemQrService systemQrService;

    @GetMapping("/{token}")
    @Operation(summary = "Resolver código QR por token", description = "Obtiene los metadatos de un código QR y los datos del módulo/submódulo destino en el sistema")
    public ResponseEntity<ApiResponse<SystemQrResolucionResponse>> resolverQrPorToken(@PathVariable String token) {
        SystemQrResolucionResponse data = systemQrService.resolverQrPorToken(token);
        return ResponseEntity.ok(ApiResponse.success("Información del código QR recuperada exitosamente", data));
    }
}
