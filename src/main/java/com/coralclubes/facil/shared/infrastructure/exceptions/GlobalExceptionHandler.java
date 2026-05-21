package com.coralclubes.facil.shared.infrastructure.exceptions;

import com.coralclubes.RestApiExceptionHandler;
import com.coralclubes.facil.shared.infrastructure.domain.codes.LoginResponseCode;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoWebRegistrationException;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.AuthResponseCode;
import com.coralclubes.responses.codes.DbResponseCode;
import com.coralclubes.responses.codes.GeneralResponseCode;
import com.coralclubes.utils.database.SqlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
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

    @ExceptionHandler(NoWebRegistrationException.class)
    public ResponseEntity<ApiResponse<?>> handleNoWebRegistration(NoWebRegistrationException ex) {
        String message = "El usuario no tiene un registro web asociado: " + ex.getMessage();
        businessLogger.warn("SECURITY", message);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error(LoginResponseCode.NO_WEB_REGISTRATION, message)
        );
    }

    // Aqui se agregan manejadores de excepciones
    // especificos para el sistema FACIL.

    @Order(1)
    @ExceptionHandler(UncategorizedSQLException.class)
    public ResponseEntity<ApiResponse<?>> handleSqlException(UncategorizedSQLException ex) {

        String realMessage = SqlUtils.getSqlErrorMessage(ex);

        ApiResponse<?> apiResponse = ApiResponse.error(
                DbResponseCode.QUERY_ERROR,
                realMessage
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

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

    @Order(1)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        ApiResponse<?> apiResponse = ApiResponse.error(AuthResponseCode.BAD_CREDENTIALS, ex.getMessage());

        businessLogger.warn("SECURITY", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}