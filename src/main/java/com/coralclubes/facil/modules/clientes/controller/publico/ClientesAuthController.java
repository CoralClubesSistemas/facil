package com.coralclubes.facil.modules.clientes.controller.publico;

import com.coralclubes.facil.modules.clientes.dto.projection.ClienteValidacionMembresiaResult;
import com.coralclubes.facil.modules.clientes.dto.request.ClienteRegistroRequest;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.dto.response.ValidacionCorreoDto;
import com.coralclubes.facil.modules.clientes.service.ClientesRegistrationService;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/clientes")
@RequiredArgsConstructor
public class ClientesAuthController {

    private final ClientesRegistrationService clientesRegistrationService;
    private final SociosService service;

    @GetMapping("/validar-membresia")
    public ResponseEntity<ApiResponse<Boolean>> validarMembresia(
            @RequestParam String membresia,
            @RequestParam String email
            ) {
        Boolean resultado = clientesRegistrationService.registroValido(membresia, email);
        return ResponseEntity.ok(ApiResponse.success(resultado));
    }

    @PostMapping("/crear-acceso")
    public ResponseEntity<ApiResponse<Void>> crearAccesoWeb(@Valid @RequestBody ClienteRegistroRequest request) {
        clientesRegistrationService.crearAccesoWeb(request);
        return ResponseEntity.ok(ApiResponse.success("Acceso web creado exitosamente", null));
    }

    @GetMapping("/validar-codigo")
    public ResponseEntity<ApiResponse<Boolean>> validarCodigo(
            @RequestParam String membresia,
            @RequestParam String codigo
    ) {
        Boolean resultado = clientesRegistrationService.validarCodigoVerificacion(membresia, codigo);
        return ResponseEntity.ok(ApiResponse.success(resultado));
    }

    @PostMapping("/reenviar-codigo")
    public ResponseEntity<ApiResponse<Boolean>> reenviarCodigo(
            @RequestParam String membresia,
            @RequestParam String email
    ) {
        Boolean resultado = clientesRegistrationService.reenviarCodigo(membresia, email);
        return ResponseEntity.ok(ApiResponse.success("Código reenviado exitosamente", resultado));
    }

    @GetMapping("/validar-correo")
    public ResponseEntity<ApiResponse<ValidacionCorreoDto>> validarCorreoExistente(
            @RequestParam String correo
    ) {
        ValidacionCorreoDto resultado = clientesRegistrationService.validarCorreoExistente(correo);
        return ResponseEntity.ok(ApiResponse.success("Validación de correo completada", resultado));
    }
}
