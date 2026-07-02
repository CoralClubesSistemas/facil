package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.EmailRequestDto;
import com.coralclubes.facil.modules.cobranza.service.EmailService;
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
@RequestMapping("/api/v1/admin/cobranza/emails")
@RequiredArgsConstructor
public class EmailsAdminController {

    private final EmailService emailService;

    @PostMapping("/enviar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Boolean>> enviarCorreo(@Valid @RequestBody EmailRequestDto request) {
        emailService.enviarCorreo(request);
        return ResponseEntity.ok(ApiResponse.success("Correo enviado correctamente.", true));
    }
}
