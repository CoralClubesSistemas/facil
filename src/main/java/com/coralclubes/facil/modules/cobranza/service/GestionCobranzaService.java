package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.GenerarGestionCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.projection.GenerarGestionCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.GestionCobranzaLink;
import com.coralclubes.facil.modules.cobranza.repository.GestionCobranzaRepository;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GestionCobranzaService {
    private final GestionCobranzaRepository repository;
    private final ObjectMapper objectMapper;
    private final UserContext userContext;

    @Value("${app.url.portal-socios}")
    private String baseUrlPortal;

    public ApiResponse<GestionCobranzaLink> generarGestionCobranza(GenerarGestionCobranzaRequest request) {
        String usuario = userContext.getUsername();
        String movimientosJson = serializarMovimientos(request);

        GenerarGestionCobranzaResponse response = repository
                .spCobranzaGenerarGestionCobranza(
                        request.membresia(),
                        usuario,
                        request.fechaInicioVigencia(),
                        request.fechaFinVigencia(),
                        request.habilitarMeses() != null && request.habilitarMeses(),
                        movimientosJson
                )
                .orElseThrow(() -> new IllegalStateException("No se pudo generar la gestión de cobranza."));

        String link = baseUrlPortal + "/" + response.tokenPagoEnLinea();
        GestionCobranzaLink gestionLink = new GestionCobranzaLink(link);

        return ApiResponse.success("Gestión de cobranza generada correctamente.", gestionLink);
    }

    private String serializarMovimientos(GenerarGestionCobranzaRequest request) {
        try {
            return objectMapper.writeValueAsString(request.movimientos());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el detalle de movimientos para la gestión de cobranza.");
        }
    }
}

