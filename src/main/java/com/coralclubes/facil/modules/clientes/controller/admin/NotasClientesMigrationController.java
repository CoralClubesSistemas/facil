package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.migration.NotasClientesMigrationService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para administrar y disparar manualmente procesos relacionados
 * con la migración de imágenes en Base64 de notas de clientes.
 */
@RestController
@RequestMapping("/api/v1/admin/clientes/socios/notas/migracion")
@RequiredArgsConstructor
public class NotasClientesMigrationController {

    private final NotasClientesMigrationService migrationService;

    /**
     * Endpoint para disparar de forma manual y asíncrona la migración de imágenes.
     * Retorna de manera inmediata al cliente con un estado OK (200) mientras el proceso
     * corre en segundo plano.
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse<String>> ejecutarMigracion() {
        migrationService.ejecutarMigracionAsync();
        return ResponseEntity.ok(ApiResponse.success(
                "Proceso de migración de imágenes de notas de clientes iniciado en segundo plano exitosamente.",
                "MIGRACION_INICIADA"
        ));
    }
}
