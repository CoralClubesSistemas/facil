package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.MovimientoAfectadoCancelacionDto;
import com.coralclubes.facil.modules.cobranza.dto.request.CancelarReciboRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.BuscarRecibosResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.ObtenerDetallesReciboResponse;
import com.coralclubes.facil.modules.cobranza.repository.RecibosRepository;
import com.coralclubes.facil.shared.events.dto.ReciboCanceladoEvent;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecibosService {

    private final RecibosRepository recibosRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserContext userContext;

    /**
     * Busca recibos de cobranza con múltiples filtros opcionales.
     *
     * @param folioRecibo Formato: numero-serieDescripcion
     * @param fechaGeneracionDe Fecha desde (ISO 8601)
     * @param fechaGeneracionA Fecha hasta (ISO 8601)
     * @param membresia Identificador de membresía
     * @param desarrolloId ID del desarrollo
     * @param usuario Código de usuario que generó el recibo
     * @param nombreSocio Búsqueda en nombre completo del cliente
     * @param terminacionTarjeta Últimos dígitos de tarjeta (si aplica)
     * @param filtrarPorEstatus 1 = solo Generado (684), 0 = múltiples estatus
     * @return Respuesta con lista de recibos encontrados
     */
    public ApiResponse<List<BuscarRecibosResponse>> buscarRecibos(
            String folioRecibo,
            LocalDate fechaGeneracionDe,
            LocalDate fechaGeneracionA,
            String membresia,
            Integer desarrolloId,
            String usuario,
            String nombreSocio,
            String terminacionTarjeta,
            Boolean filtrarPorEstatus
    ) {
        List<BuscarRecibosResponse> resultados = recibosRepository.spCobranzaBuscarRecibos(
                folioRecibo,
                fechaGeneracionDe,
                fechaGeneracionA,
                membresia,
                desarrolloId,
                usuario,
                nombreSocio,
                terminacionTarjeta,
                filtrarPorEstatus
        );
        return ApiResponse.success("Recibos encontrados correctamente.", resultados);
    }

    public ApiResponse<ObtenerDetallesReciboResponse> obtenerDetallesRecibo(
            Integer numeroRecibo,
            Integer serieReciboId,
            String membresia
    ) {
        String detallesJson = recibosRepository
                .spCobranzaObtenerDetallesRecibo(numeroRecibo, serieReciboId, membresia)
                .orElseThrow(() -> new IllegalStateException("No se encontraron detalles para el recibo solicitado."));

        if (detallesJson.isBlank()) {
            throw new IllegalStateException("La consulta de detalles de recibo regresó un JSON vacío.");
        }

        try {
            ObtenerDetallesReciboResponse response = objectMapper.readValue(detallesJson, ObtenerDetallesReciboResponse.class);
            return ApiResponse.success("Detalles de recibo obtenidos correctamente.", response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de detalles del recibo.");
        }
    }

    @Transactional
    public ApiResponse<Boolean> cancelarRecibo(CancelarReciboRequest request) {
        String usuario = userContext.getUsername();

        // 1. Ejecutar rollback contable en BD
        List<MovimientoAfectadoCancelacionDto> afectados = recibosRepository.spCobranzaCancelarRecibo(
                request.membresia(),
                request.numeroRecibo(),
                request.serieReciboId(),
                usuario,
                request.razonCancelacion()
        );

        // 2. Disparar el evento de dominio (Orquestación desacoplada)
        // El resto de los módulos (Reservas, Puntos) estarán escuchando este record
        eventPublisher.publishEvent(new ReciboCanceladoEvent(
                request.membresia(),
                usuario,
                afectados
        ));

        return ApiResponse.success("Recibo cancelado exitosamente.", true);
    }
}

