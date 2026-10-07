package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto;

/**
 * Resultado genérico de análisis de archivos con metadatos del motor utilizado.
 *
 * @param datos        Datos parseados al tipo solicitado por el módulo consumidor.
 * @param motorUsado   Motor utilizado para el análisis (DIGITAL_PDF, BEDROCK_AI o REMOTE_SERVICE).
 * @param advertencias Advertencias o notas sobre la extracción (opcional).
 * @param <T>          Tipo de objeto resultante.
 */
public record ResultadoAnalisis<T>(
        T datos,
        MotorAnalisis motorUsado,
        String advertencias
) {
    public static <T> ResultadoAnalisis<T> digital(T datos) {
        return new ResultadoAnalisis<>(datos, MotorAnalisis.DIGITAL_PDF, null);
    }

    public static <T> ResultadoAnalisis<T> ai(T datos) {
        return new ResultadoAnalisis<>(datos, MotorAnalisis.BEDROCK_AI, null);
    }

    public static <T> ResultadoAnalisis<T> ai(T datos, String advertencias) {
        return new ResultadoAnalisis<>(datos, MotorAnalisis.BEDROCK_AI, advertencias);
    }

    public static <T> ResultadoAnalisis<T> remote(T datos) {
        return new ResultadoAnalisis<>(datos, MotorAnalisis.REMOTE_SERVICE, null);
    }
}
