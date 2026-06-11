package com.coralclubes.facil.modules.clientes.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Planificador encargado de ejecutar el proceso de migración de imágenes
 * de notas de clientes de forma automática diariamente a las 3:00 AM.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotasClientesMigrationScheduler {

    private final NotasClientesMigrationService migrationService;

    /**
     * Cron para ejecutarse todos los días a las 3:00 AM.
     * formato: segundo minuto hora día_mes mes día_semana
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void ejecutarMigracionDiaria() {
        log.info("Iniciando ejecución programada diaria de la migración de imágenes (3:00 AM)...");
        try {
            migrationService.ejecutarMigracion();
            log.info("Ejecución programada diaria finalizada con éxito.");
        } catch (Exception e) {
            log.error("Error crítico durante la ejecución programada diaria de la migración: {}", e.getMessage(), e);
        }
    }
}
