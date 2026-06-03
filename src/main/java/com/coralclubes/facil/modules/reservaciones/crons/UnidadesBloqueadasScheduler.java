package com.coralclubes.facil.modules.reservaciones.crons;

import com.coralclubes.facil.modules.reservaciones.dto.request.ReactivarUnidadRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.UnidadBloqueadaDto;
import com.coralclubes.facil.modules.reservaciones.repository.UnidadesRepository;
import com.coralclubes.facil.modules.reservaciones.service.UnidadesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Daemon/Cron class to automatically process blocked units whose block period ends today.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnidadesBloqueadasScheduler {

    private final UnidadesRepository unidadesRepository;
    private final UnidadesService unidadesService;

    /**
     * Executes daily at 3:00 AM.
     * Fetches all blocked units, checks if their block ends today, and reactivates them.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void reactivarUnidadesBloqueadasExpiradas() {
        log.info("Starting cron job: reactivarUnidadesBloqueadasExpiradas");
        try {
            // Passing 0 returns all blocked units across all developments
            List<UnidadBloqueadaDto> blockedUnits = unidadesRepository.obtenerUnidadesBloqueadas(0);
            if (blockedUnits == null || blockedUnits.isEmpty()) {
                log.info("No blocked units found in the system.");
                return;
            }

            LocalDate today = LocalDate.now();
            log.info("Processing {} blocked units to check for expiration today ({})...", blockedUnits.size(), today);

            int reactivatedCount = 0;
            for (UnidadBloqueadaDto unit : blockedUnits) {
                if (unit.fechaFin() != null && unit.fechaFin().equals(today)) {
                    log.info("Reactivating unit physical ID: {}, Unit Number: {}, Block End Date: {}",
                            unit.idUnidadFisica(), unit.numeroUnidad(), unit.fechaFin());
                    try {
                        unidadesService.reactivarUnidadFisica(new ReactivarUnidadRequest(unit.idUnidadFisica()));
                        reactivatedCount++;
                    } catch (Exception e) {
                        log.error("Failed to automatically reactivate unit physical ID: {} due to error: {}", 
                                unit.idUnidadFisica(), e.getMessage(), e);
                    }
                }
            }

            log.info("Cron job finished. Reactivated {} units.", reactivatedCount);
        } catch (Exception e) {
            log.error("Error occurred during reactivarUnidadesBloqueadasExpiradas cron job", e);
        }
    }
}
