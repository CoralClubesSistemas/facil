package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.client;

import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.AnalisisArchivoSolicitud;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.ResultadoAnalisis;

import java.util.UUID;

/**
 * Contrato público del cliente de análisis de archivos.
 * Consumido por Cobranza y cualquier otro módulo de FACIL.
 */
public interface FilesAnalysisClient {

    /**
     * Analiza un archivo a partir de una solicitud detallada con instrucciones y extractor digital opcional.
     *
     * @param solicitud Parámetros de la solicitud.
     * @param <T>       Tipo de dato de retorno.
     * @return El resultado con los datos extraídos y metadatos del motor utilizado.
     */
    <T> ResultadoAnalisis<T> analizar(AnalisisArchivoSolicitud<T> solicitud);

    /**
     * Sobrecarga conveniente para analizar con prompt e instrucciones directas.
     *
     * @param fileId              Identificador del archivo en almacenamiento.
     * @param tipoDestino         Clase destino a mapear.
     * @param promptInstrucciones Instrucciones de extracción.
     * @param <T>                 Tipo de dato de retorno.
     * @return El resultado con los datos extraídos y metadatos del motor utilizado.
     */
    default <T> ResultadoAnalisis<T> analizar(UUID fileId, Class<T> tipoDestino, String promptInstrucciones) {
        return analizar(new AnalisisArchivoSolicitud<>(fileId, tipoDestino, promptInstrucciones));
    }
}
