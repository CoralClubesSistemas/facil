package com.coralclubes.facil.shared.config;

import com.coralclubes.facil.shared.infrastructure.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Se ejecuta antes que el manejo normal de mensajes
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Solo nos interesa validar cuando el cliente intenta conectar por primera vez
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // Extraer el header Authorization de la trama STOMP
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String jwt = authHeader.substring(7);
                        String username = jwtService.getUsernameFromToken(jwt);

                        if (username != null) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                            if (jwtService.validateToken(jwt, userDetails)) {
                                // Si el token es válido, creamos el token de autenticación
                                UsernamePasswordAuthenticationToken authToken =
                                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                                // Se lo asignamos al contexto de este canal WebSocket
                                accessor.setUser(authToken);
                            } else {
                                throw new IllegalArgumentException("Token JWT expirado o inválido para WebSocket");
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("No se proporcionó token JWT en la conexión WebSocket");
                    }
                }
                return message;
            }
        });
    }
}