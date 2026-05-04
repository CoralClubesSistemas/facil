package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.GenerarGestionCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.projection.GenerarGestionCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.GestionCobranzaLink;
import com.coralclubes.facil.modules.cobranza.service.GestionCobranzaService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/cobranza/gestiones")
@RequiredArgsConstructor
public class GestionCobranzaAdminController {

    private final GestionCobranzaService service;

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<GestionCobranzaLink>> generarGestionCobranza(
            @Valid @RequestBody GenerarGestionCobranzaRequest request
    ) {
        return ResponseEntity.ok(service.generarGestionCobranza(request));
    }
}

