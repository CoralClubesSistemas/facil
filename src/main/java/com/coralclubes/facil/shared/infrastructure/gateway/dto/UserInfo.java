package com.coralclubes.facil.shared.infrastructure.gateway.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * DTO estandarizado que el gateway espera recibir de cada backend
 * tras autenticar a un usuario. El gateway usa estos datos para
 * generar el JWT y los headers X-Auth-* downstream.
 */
@Builder
@Jacksonized
public record UserInfo(
        String username,
        String email,
        String nombreCompleto,
        Integer rolId,
        String role,
        String source,
        String legacyId,
        String status,
        Integer idDesarrollo,
        String desarrolloDescripcion,
        List<String> permissions
) {}