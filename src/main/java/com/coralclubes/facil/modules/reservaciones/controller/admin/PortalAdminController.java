package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarExperienciaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarImagenPortalCompraMembresiaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.ExperienciaPortalDto;
import com.coralclubes.facil.modules.reservaciones.service.PortalService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reservaciones/portal")
@RequiredArgsConstructor
public class PortalAdminController {

    private final PortalService service;
    private final UserContext userContext;

    @GetMapping("/experiencias")
    @PreAuthorize("hasAuthority('MOD_SMNUCONFIGURACIONPORTAL')")
    public ResponseEntity<ApiResponse<List<ExperienciaPortalDto>>> obtenerExperienciasPortal() {
        List<ExperienciaPortalDto> experiencias = service.obtenerExperienciasPortal();
        return ResponseEntity.ok(ApiResponse.success(experiencias));
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAuthority('MOD_SMNUCONFIGURACIONPORTAL')")
    public ResponseEntity<ApiResponse<Integer>> guardarExperiencia(
            @Valid @RequestBody GuardarExperienciaRequest request) {
        String usuario = userContext.getUsername();
        Integer id = service.guardarExperiencia(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Experiencia guardada exitosamente", id));
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAuthority('MOD_SMNUCONFIGURACIONPORTAL')")
    public ResponseEntity<ApiResponse<Void>> eliminarExperiencia(@PathVariable Integer id) {
        String usuario = userContext.getUsername();
        service.eliminarExperiencia(id, usuario);
        return ResponseEntity.ok(ApiResponse.success("Experiencia eliminada exitosamente", null));
    }

    @PostMapping("/imagenes/upload-url")
    @PreAuthorize("hasAuthority('MOD_SMNUCONFIGURACIONPORTAL')")
    public ResponseEntity<ApiResponse<RespuestaCargaDto>> solicitarUrlCarga(
            @Valid @RequestBody SolicitarUrlRequest request) {
        String usuario = userContext.getUsername();
        RespuestaCargaDto respuesta = service.solicitarUrlCarga(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("URL de carga generada exitosamente", respuesta));
    }

    @GetMapping("/compra-membresia/imagen")
    @PreAuthorize("hasAuthority('MOD_SMNUCONFIGURACIONPORTAL')")
    public ResponseEntity<ApiResponse<String>> obtenerImagenPortalCompraMembresia() {
        String url = service.obtenerImagenPortalCompraMembresia();
        return ResponseEntity.ok(ApiResponse.success(url));
    }

    @PostMapping("/compra-membresia/imagen")
    @PreAuthorize("hasAuthority('MOD_SMNUCONFIGURACIONPORTAL')")
    public ResponseEntity<ApiResponse<Void>> guardarImagenPortalCompraMembresia(
            @Valid @RequestBody GuardarImagenPortalCompraMembresiaRequest request) {
        String usuario = userContext.getUsername();
        service.guardarImagenPortalCompraMembresia(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Imagen del portal de compra de membresía guardada exitosamente", null));
    }
}
