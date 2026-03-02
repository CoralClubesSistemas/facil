package com.coralclubes.facil.modules.sistema.controller;

import com.coralclubes.facil.modules.sistema.dto.response.ModuloApiResponse;
import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.modules.sistema.service.ModulosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador Administrativo para gestionar los módulos del sistema.
 * Expone endpoints RESTful protegidos por permisos específicos.
 * 100% exclusivo del sistema interno (Facil Core).
 */
@RestController
@RequestMapping("/api/v1/admin/sistema/modulos")
@RequiredArgsConstructor
public class ModulosAdminController {

    private final ModulosService modulosService;

    /**
     * Obtiene la jerarquía completa de módulos.
     * Permiso: ACCESS_RMDL (Read Modulo)
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ACCESS_RMDL')")
    public ResponseEntity<ApiResponse<List<ModuloApiResponse>>> obtenerTodosLosModulos() {
        ApiResponse<List<ModuloApiResponse>> response = modulosService.obtenerTodosLosModulos();
        return ResponseEntity.status(response.status()).body(response);
    }

    /**
     * Crea o actualiza un módulo.
     * Permiso: ACCESS_CMDL (Create) o ACCESS_UMDL (Update)
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ACCESS_CMDL') or hasAuthority('ACCESS_UMDL')")
    public ResponseEntity<ApiResponse<Integer>> crearModulo(@RequestBody ModuloDtoResult modulo) {
        ApiResponse<Integer> response = modulosService.guardarModulo(modulo);
        return ResponseEntity.status(response.status()).body(response);
    }

    /**
     * Elimina un módulo por su ID.
     * Permiso: ACCESS_DMDL (Delete)
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('ACCESS_DMDL')")
    public ResponseEntity<ApiResponse<Integer>> eliminarModulo(@RequestParam Integer moduloId) {
        ApiResponse<Integer> response = modulosService.eliminarModulo(moduloId);
        return ResponseEntity.status(response.status()).body(response);
    }
}