package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto;

/**
 * Listado de motores de análisis de archivos disponibles.
 * DIGITAL_PDF: Motor interno para PDFs. Usa PDFBox y reglas de extracción mediante expresiones regulares.
 * BEDROCK_AI: Motor externo para PDFs. Usa Bedrock Vision para extraer texto y datos estructurados.
 * REMOTE_SERVICE: Motor externo para cualquier tipo de archivo. Usa un servicio remoto que puede ser un microservicio o API de terceros.
 */
public enum MotorAnalisis {
    DIGITAL_PDF,
    BEDROCK_AI,
    REMOTE_SERVICE
}
