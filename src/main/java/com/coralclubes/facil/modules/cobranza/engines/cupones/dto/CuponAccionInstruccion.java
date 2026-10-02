package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.util.Collections;
import java.util.Map;

/**
 * Representa una acción o instrucción resultante de la aplicación de un cupón.
 * Cada implementacion o consumo debe implementar la logica de accion a partir del valor recibido
 *
 * @param tipoAccion       El tipo de acción a realizar. Generalmente una key que identifica la acción a ejecutar (Ej. "APLICAR_DESCUENTO", "AGREGAR_PRODUCTO", etc.).
 * @param conceptoObjetivo El concepto objetivo de la acción.
 * @param cantidad         La cantidad de la acción.
 * @param metadata         Metadatos adicionales de la acción. Cada estrategia puede definir los campos que considere necesario pasar al consumidor.
 */
public record CuponAccionInstruccion(
        String tipoAccion,
        String conceptoObjetivo,
        Integer cantidad,
        Map<String, Object> metadata
) {
    public CuponAccionInstruccion {
        if (metadata == null) {
            metadata = Collections.emptyMap();
        }
    }
}
