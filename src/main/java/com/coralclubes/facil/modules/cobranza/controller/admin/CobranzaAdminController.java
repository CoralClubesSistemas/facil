package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.GenerarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
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
@RequestMapping("/api/v1/admin/cobranza")
@RequiredArgsConstructor
public class CobranzaAdminController {

    private final CobranzaService cobranzaService;
    private final UserContext userContext;

    @PostMapping("/ordenes/generar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<GenerarOrdenCobranzaResponse>> generarOrdenCobranza(
            @Valid @RequestBody GenerarOrdenCobranzaRequest request
    ) {
        String username = userContext.getUsername();

        return ResponseEntity.ok(cobranzaService.generarOrdenCobranza(request, username));
    }
}

