package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public record CuponEvaluacionContexto(
        String membresia,
        Integer idDesarrollo,
        LocalDate fechaConsumo,
        BigDecimal montoOriginal,
        String conceptoObjetivo,
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
