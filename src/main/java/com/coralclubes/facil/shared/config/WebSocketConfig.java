package com.coralclubes.facil.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUser;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPass;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Este es el endpoint HTTP al que Angular hará el Upgrade a WebSocket
        registry.addEndpoint("/ws-api")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. Rutas destino hacia el frontend (Broker Relay hacia RabbitMQ)
        // Spring delega el manejo de estas rutas a RabbitMQ a través del puerto STOMP interno (61613)
        registry.enableStompBrokerRelay("/topic", "/queue", "/exchange")
                .setRelayHost(rabbitHost)
                .setRelayPort(61613) // Puerto TCP nativo STOMP de RabbitMQ
                .setClientLogin(rabbitUser)
                .setClientPasscode(rabbitPass)
                .setSystemLogin(rabbitUser)
                .setSystemPasscode(rabbitPass);

        // 3. Prefijo para mensajes que el Frontend envía HACIA el Backend
        registry.setApplicationDestinationPrefixes("/app");

        // 4. Prefijo para mensajes dirigidos a destinos específicos de usuarios (p. ej., respuestas privadas)
        registry.setUserDestinationPrefix("/user");
    }
}