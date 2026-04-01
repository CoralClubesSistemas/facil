package com.coralclubes.facil.modules.sistema.controller;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.modules.sistema.dto.response.ModuloApiResponse;
import com.coralclubes.facil.modules.sistema.service.ModulosService;
import com.coralclubes.facil.shared.infrastructure.codes.LoginResponseCode;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoPermissionsException;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador para obtener los módulos del menú del usuario actual.
 * El gateway valida el JWT e inyecta X-Auth-Username header.
 */
@RestController
@RequestMapping("/api/v1/admin/modulos")
@RequiredArgsConstructor
public class ModulosUsuarioController {

    private final LoginRepository loginRepository;
    private final ModulosService modulosService;
    private final UserContext userContext;

    /**
     * Obtiene los módulos asignados al usuario autenticado (árbol jerárquico).
     * El username se extrae del header X-Auth-Username inyectado por el gateway.
     */
    @GetMapping("/usuario")
    public ResponseEntity<ApiResponse<List<ModuloApiResponse>>> getModulosUsuario() {
        String username = userContext.getUsername();

        List<ModuloDtoResult> userResults = loginRepository.spLoginModulosUsuarios(username);
        if (userResults.isEmpty()) {
            throw new NoPermissionsException("El usuario no tiene módulos asignados");
        }

        return ResponseEntity.ok(
                ApiResponse.from(LoginResponseCode.LOGIN_MODULES_CONSTRAINT, modulosService.getFormatModules(userResults))
        );
    }
}
