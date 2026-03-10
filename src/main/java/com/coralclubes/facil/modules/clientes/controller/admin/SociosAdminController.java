package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/socios")
@RequiredArgsConstructor
public class SociosAdminController {
    private final SociosService service;

    @GetMapping("/busqueda/{busqueda}")
    public ResponseEntity<ApiResponse<List<InformacionSocio>>> obtenerSocioPorMembresia(
            @PathVariable String busqueda
    ) {
        ApiResponse<List<InformacionSocio>> response = service.obtenerSocios(busqueda);
        return ResponseEntity.status(response.status()).body(response);
    }
}
