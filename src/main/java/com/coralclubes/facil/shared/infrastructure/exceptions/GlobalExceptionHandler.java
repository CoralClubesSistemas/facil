package com.coralclubes.facil.shared.infrastructure.exceptions;

import com.coralclubes.RestApiExceptionHandler;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.AuthResponseCode;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controlador global para manejar excepciones en la aplicacion FACIL.
 * Extiende la funcionalidad del manejador de excepciones REST
 * proporcionado por la libreria compartida CoralClubes.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends RestApiExceptionHandler {
    private final BusinessLogger businessLogger;

    // Aqui se agregan manejadores de excepciones
    // especificos para el sistema FACIL.

    /**
     * Manejador para IllegalStateException.
     * Esta excepcion indica que el sistema se encuentra en un estado
     * no valido para la operacion solicitada.
     *
     * @param ex La excepcion lanzada.
     * @return ResponseEntity con el ApiResponse de error y estado CONFLICT.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalState(IllegalStateException ex) {
        var resp = "El sistema se encuentra en un estado no válido: " + ex.getMessage();
        businessLogger.error("SYSTEM", resp);

        ApiResponse<?> response = ApiResponse.error(
                GeneralResponseCode.INTERNAL_SERVER_ERROR,
                resp
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        ApiResponse<?> apiResponse = ApiResponse.error(AuthResponseCode.BAD_CREDENTIALS, "Usuario o contraseña incorrectos");

        businessLogger.warn("SECURITY", "Intento de login fallido: Credenciales inválidas.");

        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }
}