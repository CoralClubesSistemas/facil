package com.coralclubes.facil.shared.infrastructure.domain.dto;

/**
 * Dto generico diseñado para ser compatible con componentes de selección en el frontend, como dropdowns o selectores.
 * Contiene dos campos: 'value' que representa el valor interno o identificador del elemento,
 * y 'label' que es la representación legible para el usuario.
 */
public record SelectGenerico<T>(
        T value,
        String label
) {
}
