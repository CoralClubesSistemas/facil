package com.coralclubes.facil.shared.platform.templating.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP para comunicarse con el contenedor Gotenberg
 * y transformar HTML en arreglos de bytes de PDF.
 */
@Component
public class GotenbergClient {

    private final RestClient restClient;

    public GotenbergClient(@Value("${app.clients.gotenberg.url:http://localhost:3000}") String gotenbergUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000); // 10 segundos para conectar
        requestFactory.setReadTimeout(60000);    // 60 segundos para esperar el renderizado del PDF

        this.restClient = RestClient.builder()
                .baseUrl(gotenbergUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Envía el HTML plano al endpoint /forms/chromium/convert/html de Gotenberg
     * para generar un archivo PDF nativo usando Chromium sin cabeza.
     *
     * @param html Contenido HTML compilado por Pebble
     * @return Arreglo de bytes del PDF generado
     */
    public byte[] convertHtmlToPdf(String html) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Gotenberg requiere que el archivo principal se llame exactamente 'index.html'
            ByteArrayResource htmlResource = new ByteArrayResource(html.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return "index.html";
                }
            };
            body.add("files", htmlResource);

            // Opciones de Chromium para la conversión
            body.add("marginTop", "0.39");    // 1cm en pulgadas aprox
            body.add("marginBottom", "0.39"); // 1cm en pulgadas aprox
            body.add("marginLeft", "0.47");   // 1.2cm en pulgadas aprox
            body.add("marginRight", "0.47");  // 1.2cm en pulgadas aprox
            body.add("printBackground", "true");
            body.add("paperWidth", "8.5");    // Carta / Letter
            body.add("paperHeight", "11.0");

            return restClient.post()
                    .uri("/forms/chromium/convert/html")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(byte[].class);

        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el microservicio de Gotenberg: " + e.getMessage(), e);
        }
    }
}
