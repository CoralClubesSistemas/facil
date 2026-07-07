package com.coralclubes.facil.modules.sistema.scheduler;

import com.coralclubes.facil.modules.sistema.service.LimpiezaMovimientosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LimpiezaMovimientosScheduler {

    private final LimpiezaMovimientosService service;

    /**
     * Se ejecuta todos los días a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarMovimientosInternet() {
        log.info("Iniciando cron job: limpiarMovimientosInternet");
        try {
            service.limpiarMovimientosInternet();
            log.info("Cron job terminado: limpiarMovimientosInternet completado exitosamente");
        } catch (Exception e) {
            log.error("Error al ejecutar el cron job limpiarMovimientosInternet: {}", e.getMessage(), e);
        }
    }
}
