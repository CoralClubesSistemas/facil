package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.EmailRequestDto;
import com.coralclubes.facil.modules.cobranza.dto.request.SintetizarCuerpoCorreoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuerpoCorreoResponse;
import com.coralclubes.facil.modules.cobranza.service.EmailService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cobranza/emails")
@RequiredArgsConstructor
public class EmailsAdminController {

    private final EmailService emailService;
    private final UserContext userContext;

    @PostMapping("/enviar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Boolean>> enviarCorreo(@Valid @RequestBody EmailRequestDto request) {
        String username = userContext.getUsername();
        emailService.enviarCorreo(request, username);
        return ResponseEntity.ok(ApiResponse.success("Correo enviado correctamente.", true));
    }

    @PostMapping("/sintetizar-cuerpo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<CuerpoCorreoResponse>> sintetizarCuerpo(
            @RequestParam String membresia,
            @Valid @RequestBody SintetizarCuerpoCorreoRequest request) {
        CuerpoCorreoResponse response = emailService.sintetizarCuerpoCorreo(membresia, request);
        return ResponseEntity.ok(ApiResponse.success("Cuerpo de correo sintetizado y renderizado correctamente.", response));
    }
}
