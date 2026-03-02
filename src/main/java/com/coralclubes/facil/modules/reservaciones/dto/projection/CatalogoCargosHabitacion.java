package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;
import java.math.BigDecimal;

/**
 * DTO para el catálogo de Cargos a Habitación que incluye el precio (Cuota).
 * Utilizado por: spResvCatalogoCargosHabitacion
 */
@Builder
public record CatalogoCargosHabitacion(
        Integer tipoMovimientoId,
        String descripcion,
        BigDecimal cuota
) {}