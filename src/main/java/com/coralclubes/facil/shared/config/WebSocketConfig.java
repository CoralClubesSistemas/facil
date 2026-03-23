package com.coralclubes.facil.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import com.coralclubes.facil.shared.infrastructure.security.service.JwtService;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUser;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPass;

    private final JwtService jwtService;

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

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Solo nos interesa el momento exacto en que Angular intenta conectarse (CONNECT)
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // 1. Extraer el header Authorization (Angular lo manda en connectHeaders)
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        try {
                            // 2. Validar el token y obtener el objeto Authentication de Spring Security
                            Authentication authentication = jwtService.getAuthentication(token);

                            accessor.setUser(authentication);

                        } catch (Exception e) {
                            // Si el token es inválido o expiró, rechazamos la conexión del WebSocket
                            throw new IllegalArgumentException("Token de WebSocket inválido o expirado");
                        }
                    } else {
                        throw new IllegalArgumentException("Falta cabecera Authorization en STOMP CONNECT");
                    }
                }
                return message;
            }
        });
    }
}