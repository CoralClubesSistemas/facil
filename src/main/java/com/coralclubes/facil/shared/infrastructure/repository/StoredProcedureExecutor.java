package com.coralclubes.facil.shared.infrastructure.repository;

import com.coralclubes.logging.SqlLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Componente genérico para la ejecución segura y estandarizada de Stored Procedures.
 * Centraliza el manejo de logs (SqlLogger), excepciones y mapeo a DTOs.
 */
@Component
@RequiredArgsConstructor
public class StoredProcedureExecutor {

    private final JdbcTemplate jdbcTemplate;
    private final SqlLogger sqlLogger;

    private static final String DEFAULT_USER = "SYSTEM";

    // =========================================================================
    // 1. QUERY LIST (Retorna Lista)
    // =========================================================================

    // Sobrecarga Básica (Logs estándar: Params SI, Success NO, User SYSTEM)
    public <T> List<T> queryListLog(
            String spName,
            Map<String, Object> params,
            RowMapper<T> rowMapper
    ) {
        return queryListLog(spName, params, rowMapper, DEFAULT_USER, true, false);
    }

    // Sobrecarga para Ocultar Parámetros (Ej: Passwords)
    public <T> List<T> queryList(
            String spName,
            Map<String, Object> params,
            RowMapper<T> rowMapper,
            boolean logParams
    ) {
        return queryListLog(spName, params, rowMapper, DEFAULT_USER, logParams, false);
    }

    // Sobrecarga Completa (Control Total)
    public <T> List<T> queryListLog(
            String spName, // Nombre del Stored Procedure
            Map<String, Object> params,  // Parámetros de entrada (nombre-valor)
            RowMapper<T> rowMapper, // Mapeador de filas a DTOs
            String user,  // Usuario que ejecuta la SP (para logs)
            boolean logParams,  // Indica si se deben loggear los parámetros
            boolean logSuccess // Indica si se debe loggear el éxito de la operación
    ) {
        logStart(spName, params, user, logParams);

        try {
            // Configuramos y ejecutamos la llamada al Stored Procedure
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(spName)
                    .returningResultSet("result", rowMapper);

            Map<String, Object> result = simpleJdbcCall.execute(new MapSqlParameterSource(params));

            // Logueamos el éxito si está habilitado
            if (logSuccess) {
                sqlLogger.logSuccess(spName);
            }

            // Devolvemos la lista mapeada o una lista vacía si no hay resultados
            return (List<T>) result.getOrDefault("result", Collections.emptyList());
        } catch (DataAccessException ex) {
            handleError(spName, ex);
            throw ex;
        }
    }

    /**
     * Ejecuta un Stored Procedure que retorna una lista de resultados
     * sin loguear la información de la ejecución (parámetros, usuario, éxito).
     */
    public <T> List<T> queryList(
            String spName, // Nombre del Stored Procedure
            Map<String, Object> params,  // Parámetros de entrada (nombre-valor)
            RowMapper<T> rowMapper // Mapeador de filas a DTOs
    ) {
        try {
            // Configuramos y ejecutamos la llamada al Stored Procedure
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(spName)
                    .returningResultSet("result", rowMapper);

            Map<String, Object> result = simpleJdbcCall.execute(new MapSqlParameterSource(params));

            // Devolvemos la lista mapeada o una lista vacía si no hay resultados
            return (List<T>) result.getOrDefault("result", Collections.emptyList());
        } catch (DataAccessException ex) {
            handleError(spName, ex);
            throw ex;
        }
    }

    // =========================================================================
    // 2. QUERY SINGLE (Retorna Optional)
    // =========================================================================

    // Sobrecarga Básica
    public <T> Optional<T> querySingleLog(
            String spName,
            Map<String, Object> params,
            RowMapper<T> rowMapper
    ) {
        return querySingleLog(spName, params, rowMapper, DEFAULT_USER, true, false);
    }

    // Sobrecarga para Ocultar Parámetros
    public <T> Optional<T> querySingleLog(
            String spName,
            Map<String, Object> params,
            RowMapper<T> rowMapper,
            boolean logParams
    ) {
        return querySingleLog(spName, params, rowMapper, DEFAULT_USER, logParams, false);
    }

    // Sobrecarga Completa
    public <T> Optional<T> querySingleLog(
            String spName,
            Map<String, Object> params,
            RowMapper<T> rowMapper,
            String user,
            boolean logParams,
            boolean logSuccess
    ) {
        List<T> results = queryListLog(spName, params, rowMapper, user, logParams, logSuccess);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public <T> Optional<T> querySingle(
            String spName,
            Map<String, Object> params,
            RowMapper<T> rowMapper
    ) {
        List<T> results = queryList(spName, params, rowMapper);

        // Si la lista está vacía, devuelve empty.
        // Si tiene elementos, obtenemos el primero y usamos ofNullable
        return results.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(results.getFirst());
    }
    // =========================================================================
    // 3. EXECUTE (Void / Boolean)
    // =========================================================================

    // Sobrecarga Básica
    public void executeLog(
            String spName,
            Map<String, Object> params
    ) {
        executeLog(spName, params, DEFAULT_USER, true, false);
    }

    // Sobrecarga para Ocultar Parámetros y opcionalmente mostrar Success
    public void executeLog(
            String spName,
            Map<String, Object> params,
            boolean logParams,
            boolean logSuccess
    ) {
        executeLog(spName, params, DEFAULT_USER, logParams, logSuccess);
    }

    // Sobrecarga Completa
    public void executeLog(
            String spName,
            Map<String, Object> params,
            String user,
            boolean logParams,
            boolean logSuccess
    ) {
        logStart(spName, params, user, logParams);

        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(spName);

            simpleJdbcCall.execute(new MapSqlParameterSource(params));

            if (logSuccess) {
                sqlLogger.logSuccess(spName);
            }

        } catch (DataAccessException ex) {
            handleError(spName, ex);
            throw ex;
        }
    }

    public void execute(
            String spName,
            Map<String, Object> params
    ) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(spName);

            simpleJdbcCall.execute(new MapSqlParameterSource(params));

        } catch (DataAccessException ex) {
            handleError(spName, ex);
            throw ex;
        }
    }

    // =========================================================================
    // Métodos Privados de Utilidad
    // =========================================================================

    /**
     * Maneja el logueo inicial de la ejecución de un Stored Procedure.
     * Dependiendo de los flags, decide qué información loggear (parámetros, usuario, etc.) y si se debe loggear o no.
     */
    private void logStart(
            String spName,
            Map<String, Object> params,
            String user,
            boolean logParams
    ) {
        if (logParams && params != null && !params.isEmpty()) {
            // Convertimos los valores del mapa a un array para que el logger los pinte
            sqlLogger.logProcedureUser(user, spName, params.values().toArray());
        } else {
            // Si logParams es false o no hay params, mandamos un indicador protegido
            Object[] safeParams = (params == null || params.isEmpty()) ? new Object[]{} : new Object[]{"[PROTECTED_CONTENT]"};
            sqlLogger.logProcedureUser(user, spName, safeParams);
        }
    }

    // Manejo centralizado de errores
    // Loguea el error usando SqlLogger
    private void handleError(
            String spName,
            DataAccessException ex
    ) {
        sqlLogger.logError(spName, ex);
    }
}