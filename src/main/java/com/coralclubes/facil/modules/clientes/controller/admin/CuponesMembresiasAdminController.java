package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.request.AsignarCuponesMembresiaRequest;
import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleAsignacionResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaDetalleResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaResumenResponse;
import com.coralclubes.facil.modules.clientes.service.CuponesMembresiasService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/cupones-membresias")
@RequiredArgsConstructor
public class CuponesMembresiasAdminController {

    private final CuponesMembresiasService cuponesMembresiasService;
    private final UserContext userContext;

    @GetMapping("/{membresia}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponMembresiaResumenResponse>>> obtenerCuponesMembresia(
            @PathVariable String membresia,
            @RequestParam(required = false) Integer year
    ) {
        List<CuponMembresiaResumenResponse> cupones = cuponesMembresiasService.obtenerCuponesMembresia(membresia, year);
        return ResponseEntity.ok(ApiResponse.success("Cupones de la membresía obtenidos exitosamente", cupones));
    }

    @GetMapping("/{membresia}/detalle/{cuponId}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponMembresiaDetalleResponse>>> obtenerDetalleCuponMembresia(
            @PathVariable String membresia,
            @PathVariable Integer cuponId
    ) {
        List<CuponMembresiaDetalleResponse> detalle = cuponesMembresiasService.obtenerDetalleCuponMembresia(cuponId, membresia);
        return ResponseEntity.ok(ApiResponse.success("Detalle de folios del cupón obtenido exitosamente", detalle));
    }

    @GetMapping("/disponibles-asignacion")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponDisponibleAsignacionResponse>>> obtenerDisponiblesParaAsignacion(
            @RequestParam(required = false) String membresia,
            @RequestParam(required = false) Integer desarrollo,
            @RequestParam(required = false) Integer tipoMembresiaId,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaAsignacion
    ) {
        List<CuponDisponibleAsignacionResponse> disponibles = cuponesMembresiasService.obtenerDisponiblesParaAsignacion(
                membresia, desarrollo, tipoMembresiaId, anio, fechaAsignacion
        );
        return ResponseEntity.ok(ApiResponse.success("Cupones disponibles para asignación obtenidos exitosamente", disponibles));
    }

    @PostMapping("/asignar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> asignarCuponesAMembresia(
            @Valid @RequestBody AsignarCuponesMembresiaRequest request
    ) {
        String usuario = userContext.getUsername();
        cuponesMembresiasService.asignarCuponesAMembresia(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Cupones asignados a la membresía exitosamente", null));
    }
}
