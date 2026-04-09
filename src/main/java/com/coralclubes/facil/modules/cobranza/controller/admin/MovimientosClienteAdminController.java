package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCuentaAdeudoDto;
import com.coralclubes.facil.modules.cobranza.service.MovimientosClienteService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza")
@RequiredArgsConstructor
public class MovimientosClienteAdminController {

    private final MovimientosClienteService service;

    @GetMapping("/estado-cuenta-adeudo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<EstadoCuentaAdeudoDto>>> obtenerEstadoCuentaAdeudo(
            @RequestParam String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte) {

        EstadoCuentaAdeudoRequest request = EstadoCuentaAdeudoRequest.builder()
                .membresia(membresia)
                .fechaCorte(fechaCorte)
                .build();

        return ResponseEntity.ok(service.obtenerEstadoCuentaAdeudo(request));
    }
}

