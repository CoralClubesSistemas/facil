package com.coralclubes.facil.shared.infrastructure.integration.checkout;

import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.dto.CheckoutInitRequest;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.dto.CheckoutInitResponse;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CheckoutClient {

    private final BusinessLogger logger;
    private final RestClient restClient;

    @Value("${app.clients.checkout.url}")
    private String serviceUrl;

    @Value("${app.clients.checkout.api-key}")
    private String apiKey;

    public CheckoutInitResponse iniciarSesionPago(CheckoutInitRequest solicitud) {
        try {
            CheckoutInitResponse response = restClient.post()
                    .uri(serviceUrl + "/api/v1/checkout/init")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null) {
                return response;
            }

            throw new IllegalStateException("El microservicio de checkout devolvió una respuesta vacía.");
        } catch (Exception e) {
            logger.error("CHECKOUT_CLIENT", "Error al iniciar sesión de pago: " + e.getMessage(), e);
            throw new ServiceUnavailableException("El servicio de pagos (checkout) no está disponible en este momento.");
        }
    }
}
