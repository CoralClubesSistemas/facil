package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.GenerarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CobranzaService {

    private final CobranzaRepository repository;
    private final ObjectMapper objectMapper;

    public ApiResponse<GenerarOrdenCobranzaResponse> generarOrdenCobranza(GenerarOrdenCobranzaRequest request, String usuario) {
        String movimientosJson = serializarMovimientos(request);

        GenerarOrdenCobranzaResponse result = repository
                .spCobranzaGenerarOrdenCobranza(request.membresia(), usuario, movimientosJson)
                .orElseThrow(() -> new IllegalStateException("No se pudo generar la orden de cobranza."));

        return ApiResponse.success("Orden de cobranza generada correctamente.", result);
    }

    private String serializarMovimientos(GenerarOrdenCobranzaRequest request) {
        try {
            return objectMapper.writeValueAsString(request.movimientos());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el detalle de movimientos para la orden.");
        }
    }
}

