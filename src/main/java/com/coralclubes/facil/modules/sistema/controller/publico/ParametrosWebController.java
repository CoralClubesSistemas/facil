package com.coralclubes.facil.modules.sistema.controller.publico;

import com.coralclubes.facil.modules.sistema.dto.response.ParametrosWeb;
import com.coralclubes.facil.modules.sistema.service.ParametrosWebService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador para la gestión de parámetros web del sistema.
 * Expone endpoints públicos para obtener configuraciones y datos generales del aplicativo.
 */
@RestController
@RequestMapping("/api/v1/public/sistema/parametros-web")
@RequiredArgsConstructor
public class ParametrosWebController {

    private final ParametrosWebService parametrosWebService;

    @GetMapping("/version")
    public ResponseEntity<ApiResponse<String>> obtenerParametroWeb() {
        ApiResponse<String> response = parametrosWebService.obtenerParametroWebVersion();
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParametrosWeb>>> obtenerParametroWebList() {
        ApiResponse<List<ParametrosWeb>> response = parametrosWebService.obtenerParametrosWeb();
        return ResponseEntity.status(response.status()).body(response);
    }
}

