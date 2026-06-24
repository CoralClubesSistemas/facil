package com.coralclubes.facil.shared.infrastructure.gateway.controller;

import com.coralclubes.facil.shared.infrastructure.gateway.dto.UserInfo;
import com.coralclubes.facil.shared.infrastructure.gateway.service.ClientesGatewayAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth/clientes")
@RequiredArgsConstructor
public class ClientesInternalAuthController {

    private final ClientesGatewayAuthService clientesGatewayAuthService;

    @PostMapping("/login")
    public ResponseEntity<UserInfo> login(@RequestBody ClientLoginRequest request) {
        UserInfo userInfo = clientesGatewayAuthService.autenticarCliente(request);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/user/{membresia}")
    public ResponseEntity<UserInfo> getUserByMembresia(@PathVariable String membresia) {
        UserInfo userInfo = clientesGatewayAuthService.obtenerPorMembresia(membresia);
        return ResponseEntity.ok(userInfo);
    }

    public record ClientLoginRequest(
            String email,
            String password,
            String token
    ) {}
}
