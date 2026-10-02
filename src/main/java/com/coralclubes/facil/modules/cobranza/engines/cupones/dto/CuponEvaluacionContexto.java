package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Contexto de evaluación de un cupón.
 *
 * @param membresia     La membresía a la que pertenece el cupón.
 * @param idDesarrollo  El ID del desarrollo donde se está validando o aplicando el consumo.
 * @param fechaConsumo  La fecha en la que se consume el cupón.
 * @param montoOriginal El monto original del cupón antes de aplicar descuentos.
 * @param atributos     Atributos adicionales del contexto. Cada implementación o módulo puede definir los campos que considere necesario pasar al consumidor.
 */
public record CuponEvaluacionContexto(
        String membresia,
        Integer idDesarrollo,
        LocalDate fechaConsumo,
        BigDecimal montoOriginal,
        Map<String, Object> atributos
) {
    public CuponEvaluacionContexto {
        if (atributos == null) {
            atributos = Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAtributo(String clave, Class<T> clazz) {
        Object valor = atributos.get(clave);
        if (valor != null && clazz.isInstance(valor)) {
            return Optional.of((T) valor);
        }
        return Optional.empty();
    }
}
