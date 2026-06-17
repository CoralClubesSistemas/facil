package com.coralclubes.facil.modules.sistema.controller.publico;

import com.coralclubes.facil.modules.sistema.service.IconosWebService;
import com.coralclubes.facil.shared.domain.dto.IconoWeb;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/sistema/iconos-web")
@RequiredArgsConstructor
public class IconosWebController {
    private final IconosWebService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IconoWeb>>> obtenerIconosWeb() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllIconosWeb()));
    }
}
