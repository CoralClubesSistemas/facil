package com.coralclubes.facil.shared.platform.parameters.controller;

import com.coralclubes.facil.shared.platform.parameters.dto.ParametrosWeb;
import com.coralclubes.facil.shared.platform.parameters.service.ParametrosWebService;
import com.coralclubes.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/sistema/parametros-web")
@RequiredArgsConstructor
@Tag(name = "Parámetros Web", description = "Endpoints públicos para consultar parámetros generales y de configuración del aplicativo")
public class ParametrosWebController {

    private final ParametrosWebService parametrosWebService;

    @GetMapping("/version")
    @Operation(summary = "Obtener versión del sistema", description = "Retorna el parámetro web que contiene la versión actual del sistema")
    public ResponseEntity<ApiResponse<String>> obtenerParametroWebVersion() {
        ApiResponse<String> response = parametrosWebService.obtenerParametroWebVersion();
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping
    @Operation(summary = "Obtener lista de parámetros web", description = "Retorna la lista completa de parámetros web configurados en el sistema")
    public ResponseEntity<ApiResponse<List<ParametrosWeb>>> obtenerParametroWebList() {
        ApiResponse<List<ParametrosWeb>> response = parametrosWebService.obtenerParametrosWeb();
        return ResponseEntity.status(response.status()).body(response);
    }
}
