package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.request.ActualizarDatosTelefonoRequest;
import com.coralclubes.facil.modules.clientes.dto.request.TelefonoPrioridadDto;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaLlamadaResponse;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaTelefonoResponse;
import com.coralclubes.facil.modules.clientes.service.TelefonosService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/telefonos")
@RequiredArgsConstructor
public class TelefonosAdminController {

    private final TelefonosService service;

    @GetMapping("/{membresia}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MembresiaTelefonoResponse>>> obtenerNumerosTelefonos(@PathVariable String membresia) {
        List<MembresiaTelefonoResponse> telefonos = service.obtenerNumerosTelefonos(membresia);
        return ResponseEntity.ok(ApiResponse.success("Números de teléfono obtenidos exitosamente.", telefonos));
    }

    @PutMapping("/{membresia}/estatus")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> actualizarEstatusTelefono(@PathVariable String membresia, @RequestParam String numeroTelefono, @RequestParam Boolean estatus) {
        service.actualizarEstatusTelefono(membresia, numeroTelefono, estatus);
        return ResponseEntity.ok(ApiResponse.success("Estatus del teléfono actualizado exitosamente.", null));
    }

    @PutMapping("/{membresia}/datos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> actualizarDatosTelefono(@PathVariable String membresia, @RequestParam String numeroTelefono, @Valid @RequestBody ActualizarDatosTelefonoRequest request) {
        service.actualizarDatosTelefono(membresia, numeroTelefono, request);
        return ResponseEntity.ok(ApiResponse.success("Datos del teléfono actualizados exitosamente.", null));
    }

    @PutMapping("/{membresia}/prioridades")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reordenarPrioridadTelefonos(@PathVariable String membresia, @Valid @RequestBody List<TelefonoPrioridadDto> reordenamiento) {
        service.reordenarPrioridadTelefonos(membresia, reordenamiento);
        return ResponseEntity.ok(ApiResponse.success("Prioridades de teléfonos reordenadas exitosamente.", null));
    }

    @GetMapping("/{membresia}/llamadas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MembresiaLlamadaResponse>>> obtenerBitacoraLlamadas(@PathVariable String membresia) {
        List<MembresiaLlamadaResponse> llamadas = service.obtenerBitacoraLlamadas(membresia);
        return ResponseEntity.ok(ApiResponse.success("Bitácora de llamadas obtenida exitosamente.", llamadas));
    }
}
