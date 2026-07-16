package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.migration.FilesClientesMigrationService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para administrar y disparar manualmente procesos relacionados
 * con la migración de archivos y fotos en Base64 del módulo de clientes.
 */
@RestController
@RequestMapping("/api/v1/admin/clientes/migracion")
@RequiredArgsConstructor
public class FilesClientesMigrationController {

    private final FilesClientesMigrationService migrationService;

    /**
     * Endpoint para disparar de forma manual y asíncrona la migración de archivos.
     * Retorna de manera inmediata al cliente con un estado OK (200) mientras el proceso
     * corre en segundo plano.
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse<String>> ejecutarMigracion() {
        migrationService.ejecutarMigracionAsync();
        return ResponseEntity.ok(ApiResponse.success(
                "Proceso de migración de archivos de clientes iniciado en segundo plano exitosamente.",
                "MIGRACION_INICIADA"
        ));
    }
}
