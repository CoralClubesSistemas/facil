package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.repository.PaqueteAnualRepository;
import com.coralclubes.logging.BusinessLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaqueteAnualService {

    private final PaqueteAnualRepository repository;
    private final BusinessLogger businessLogger;
    private final ObjectMapper objectMapper;

    public List<MovimientoPaqueteAnualResponse> obtenerMovimientosPaqueteAnual(Integer anio, Integer tipoMembresia) {
        return repository.spCobranzaCatalogoMovimientosPaqueteAnual(anio, tipoMembresia);
    }

    public List<PaqueteAnualResponse> obtenerPaquetesAnuales(
            Integer anio,
            Integer tipoMembresia,
            Integer clasificacionMembresia,
            Integer desarrollo
    ) {
        return repository.spCobranzaObtenerPaquetesAnuales(anio, tipoMembresia, clasificacionMembresia, desarrollo);
    }

    public PaqueteAnualDetalleResponse obtenerPaqueteAnualDetalle(Integer paqueteAnualId) {
        String jsonDetalle = repository.spCobranzaObtenerPaqueteAnualDetalle(paqueteAnualId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paquete anual con ID: " + paqueteAnualId));

        try {
            return objectMapper.readValue(jsonDetalle, PaqueteAnualDetalleResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar el detalle del paquete anual", e);
        }
    }

    public List<EsquemaPagoPropuestaResponse> obtenerEsquemasPagoPropuesta(String membresia, Integer anio) {
        return repository.spCobranzaObtenerEsquemasPagoPropuestaPaqueteAnual(membresia, anio);
    }

    public CotizacionPaqueteAnualResponse cotizarPropuestaPaqueteAnual(String membresia, Integer anio, List<String> esquemas) {
        String esquemasJson;
        try {
            esquemasJson = objectMapper.writeValueAsString(esquemas != null ? esquemas : List.of());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error al serializar los esquemas a JSON", e);
        }

        String jsonCotizacion = repository.spCobranzaCotizarPropuestaPaqueteAnual(membresia, anio, esquemasJson)
                .orElseThrow(() -> new IllegalArgumentException("No se pudo generar la cotización para la membresía: " + membresia));

        try {
            return objectMapper.readValue(jsonCotizacion, CotizacionPaqueteAnualResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar la cotización del paquete anual", e);
        }
    }

    public Integer guardarPaqueteAnual(GuardarPaqueteAnualRequest request, String usuario) {
        businessLogger.info(usuario, "Solicitud de creación/actualización de paquete anual para año: {}, desarrollo: {}, tipoMembresia: {}",
                request.anio(), request.desarrollo(), request.tipoMembresia());

        String descuentosJson = null;
        if (request.configuracionDescuentos() != null) {
            try {
                descuentosJson = objectMapper.writeValueAsString(request.configuracionDescuentos());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error al serializar la configuración de descuentos a JSON", e);
            }
        }

        String movimientosJson = null;
        if (request.configuracionMovimientos() != null) {
            try {
                movimientosJson = objectMapper.writeValueAsString(request.configuracionMovimientos());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error al serializar la configuración de movimientos a JSON", e);
            }
        }

        return repository.spCobranzaGuardarPaqueteAnual(
                request.id(),
                request.anio(),
                request.tipoMembresia(),
                request.clasificacionMembresia(),
                request.desarrollo(),
                usuario,
                descuentosJson,
                movimientosJson
        ).orElseThrow(() -> new RuntimeException("Error al guardar el paquete anual en la base de datos"));
    }
}
