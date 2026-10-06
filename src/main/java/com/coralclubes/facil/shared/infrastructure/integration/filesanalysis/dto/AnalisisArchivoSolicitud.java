package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Solicitud de análisis agnóstica para cualquier módulo consumidor.
 *
 * @param fileId Identificador único del archivo en Coral Almacenamiento.
 * @param tipoDestino Clase objetivo (Java Record/POJO) a la que se desea deserializar el resultado.
 * @param promptInstrucciones Instrucciones semánticas del negocio para guiar la extracción inteligente.
 * @param extractorTextoDigital Función opcional de extracción directa sobre texto plano si el archivo es un PDF digital.
 * @param <T> Tipo de dato objetivo.
 */
public record AnalisisArchivoSolicitud<T>(
        UUID fileId,
        Class<T> tipoDestino,
        String promptInstrucciones,
        Function<String, Optional<T>> extractorTextoDigital
) {
    public AnalisisArchivoSolicitud(UUID fileId, Class<T> tipoDestino, String promptInstrucciones) {
        this(fileId, tipoDestino, promptInstrucciones, null);
    }
}
