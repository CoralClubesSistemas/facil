package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.BajaAccesoPreferencialRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.InsertarAccesoPreferencialRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.BeneficiarioAccesoVigenteResponse;
import com.coralclubes.facil.modules.cobranza.service.AccesosCobranzaService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cobranza/accesos-preferenciales")
@RequiredArgsConstructor
public class AccesosCobranzaAdminController {

    private final AccesosCobranzaService service;
    private final UserContext userContext;

    @PostMapping
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> insertarAccesoPreferencial(
            @Valid @RequestBody InsertarAccesoPreferencialRequest request
    ) {
        String usuario = userContext.getUsername();
        service.insertarAccesoPreferencial(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Acceso preferencial registrado exitosamente.", null));
    }

    @GetMapping("/vigente")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<BeneficiarioAccesoVigenteResponse>> obtenerAccesoVigente(
            @RequestParam String membresia,
            @RequestParam Integer numBeneficiario
    ) {
        BeneficiarioAccesoVigenteResponse acceso = service.obtenerAccesoVigente(membresia, numBeneficiario)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Acceso preferencial vigente consultado exitosamente.", acceso));
    }

    @PostMapping("/baja")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> bajaAccesoPreferencial(
            @Valid @RequestBody BajaAccesoPreferencialRequest request
    ) {
        String usuario = userContext.getUsername();
        service.bajaAccesoPreferencial(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Baja de acceso preferencial procesada exitosamente.", null));
    }
}
