package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.shared.infrastructure.codes.LoginResponseCode;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.SimpleLoginResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.ValidacionAutorizacion;
import com.coralclubes.facil.shared.infrastructure.security.enums.TipoAutorizacion;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio para validar autorizaciones fuera de política.
 *
 * Re-valida la identidad del usuario mediante su contraseña
 * y verifica si tiene una autorización específica asignada.
 *
 * Usado para acciones sensibles donde se requiere confirmación
 * explícita del usuario (ej. eliminar hoteles, check-in sin pago).
 */
@Service
@RequiredArgsConstructor
public class AutorizacionService {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<Boolean> validarAutorizacionFueraDePolitica(ValidacionAutorizacion request) {
        // 1. Validar credenciales (re-validación de identidad)
        SimpleLoginResult userStored = loginRepository.spLoginSimple(request.username())
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.password(), userStored.password())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // 2. Mapear la clave del enum a la clave real de la BD
        String claveRealBD;
        try {
            TipoAutorizacion tipo = TipoAutorizacion.valueOf(request.autorizacion());
            claveRealBD = tipo.getDbClave();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El tipo de autorización solicitado no existe o es inválido.");
        }

        // 3. Ejecutar SP de validación
        int validado = loginRepository.spLoginValidarAutorizacionFueraDePolitica(request.username(), claveRealBD);
        boolean esValido = validado == 1;

        return ApiResponse.from(
                esValido ? LoginResponseCode.LOGIN_AUTHORIZATION_SUCCESS : LoginResponseCode.LOGIN_NOT_PERMITIONS,
                esValido
        );
    }
}
