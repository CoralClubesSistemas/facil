package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.response.BuscarRecibosResponse;
import com.coralclubes.facil.modules.cobranza.repository.RecibosRepository;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecibosService {

    private final RecibosRepository recibosRepository;

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
}

