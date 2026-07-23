package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
import com.coralclubes.facil.modules.cobranza.service.CuponesService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza/cupones")
@RequiredArgsConstructor
public class CuponesAdminController {

    private final CuponesService cuponesService;

    @GetMapping("/catalogos/condiciones")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponesCatalogoElementoResponse>>> obtenerCatalogoCondiciones() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de condiciones obtenido exitosamente", cuponesService.obtenerCatalogoCondiciones()));
    }

    @GetMapping("/catalogos/beneficios")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponesCatalogoElementoResponse>>> obtenerCatalogoBeneficios() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de beneficios obtenido exitosamente", cuponesService.obtenerCatalogoBeneficios()));
    }

    @GetMapping("/catalogos/origenes")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoOrigenes() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de orígenes obtenido exitosamente", cuponesService.obtenerCatalogoOrigenes()));
    }

    @GetMapping("/catalogos/destinos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoDestinos() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de destinos obtenido exitosamente", cuponesService.obtenerCatalogoDestinos()));
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Integer>> guardarCupon(@Valid @RequestBody GuardarCuponRequest request) {
        Integer idCupon = cuponesService.guardarCupon(request);
        return ResponseEntity.ok(ApiResponse.success("Cupón guardado correctamente", idCupon));
    }
}
